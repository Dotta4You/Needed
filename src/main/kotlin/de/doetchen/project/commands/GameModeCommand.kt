package de.doetchen.project.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player

class GameModeCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("gm")
            .requires { it.sender is Player && it.sender.hasPermission("needed.gamemode") }
            .then(
                Commands.argument("mode", StringArgumentType.word())
                    .suggests { _, builder ->
                        listOf("0", "1", "2", "3", "survival", "creative", "adventure", "spectator").forEach {
                            builder.suggest(it)
                        }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val player = context.source.sender as Player
                        val modeInput = StringArgumentType.getString(context, "mode")

                        val gameMode = parseGameMode(modeInput) ?: return@executes 0

                        player.gameMode = gameMode

                        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
                        }

                        val modeName = gameMode.name.lowercase().replaceFirstChar { it.uppercase() }
                        val message = plugin.languageManager.getPrefix()
                            .append(plugin.languageManager.getMessage("gamemode.changed", "mode" to modeName))
                        player.sendMessage(message)

                        1
                    }
                    .then(
                        Commands.argument("target", ArgumentTypes.player())
                            .requires { it.sender.hasPermission("needed.gamemode.other") }
                            .executes { context ->
                                val sender = context.source.sender as Player
                                val modeInput = StringArgumentType.getString(context, "mode")
                                val target = context.getArgument("target", Player::class.java)

                                val gameMode = parseGameMode(modeInput) ?: return@executes 0

                                target.gameMode = gameMode

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    sender.playSound(sender.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
                                    target.playSound(target.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
                                }

                                val modeName = gameMode.name.lowercase().replaceFirstChar { it.uppercase() }
                                val senderMessage = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage(
                                        "gamemode.changed-other",
                                        "player" to target.name,
                                        "mode" to modeName
                                    ))
                                sender.sendMessage(senderMessage)

                                val targetMessage = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("gamemode.changed", "mode" to modeName))
                                target.sendMessage(targetMessage)

                                1
                            }
                    )
            )
            .build()
    }

    private fun parseGameMode(input: String): GameMode? {
        return when (input.lowercase()) {
            "0", "survival", "s" -> GameMode.SURVIVAL
            "1", "creative", "c" -> GameMode.CREATIVE
            "2", "adventure", "a" -> GameMode.ADVENTURE
            "3", "spectator", "sp" -> GameMode.SPECTATOR
            else -> null
        }
    }

    override val aliases: List<String>
        get() = listOf("gamemode")

    override val description: String
        get() = "Change gamemode"
}