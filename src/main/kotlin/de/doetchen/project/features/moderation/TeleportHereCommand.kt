package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.Sound
import org.bukkit.entity.Player

class TeleportHereCommand(private val plugin: Needed) : CommandBuilder {

    override val description = "Teleport player to you"
    override val aliases = listOf("s", "summon")

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tphere")
            .requires { it.sender is Player && it.sender.hasPermission("needed.tphere") }
            .executes { context ->
                val player = context.source.sender as Player
                val usageMessage = plugin.languageManager.getPrefix()
                    .append(net.kyori.adventure.text.Component.text(" Usage: ", net.kyori.adventure.text.format.NamedTextColor.RED))
                    .append(net.kyori.adventure.text.Component.text("/tphere <player>", net.kyori.adventure.text.format.NamedTextColor.GRAY))
                player.sendMessage(usageMessage)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val player = context.source.sender as Player
                        val target = context.getArgument("target",
                            PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()

                        if (target != null) {
                            if (target == player) {
                                val errorMessage = plugin.languageManager.getPrefix()
                                    .append(net.kyori.adventure.text.Component.text(" You cannot teleport yourself to yourself!", net.kyori.adventure.text.format.NamedTextColor.RED))
                                player.sendMessage(errorMessage)
                                return@executes 1
                            }

                            target.teleport(player.location)

                            if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                target.playSound(target.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f)
                                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.0f)
                            }

                            val targetMessage = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage(
                                    "moderation.tphere.teleported",
                                    "player" to player.name
                                ))
                            target.sendMessage(targetMessage)

                            val playerMessage = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage(
                                    "moderation.tphere.summoned",
                                    "player" to target.name
                                ))
                            player.sendMessage(playerMessage)
                        }
                        1
                    }
            )
            .build()
    }
}

