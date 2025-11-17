package de.doetchen.project.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.entity.Player

class TimeCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("time")
            .requires { it.sender is Player && it.sender.hasPermission("needed.time") }
            .then(
                Commands.argument("period", StringArgumentType.word())
                    .suggests { _, builder ->
                        listOf("day", "noon", "night", "midnight").forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val player = context.source.sender as Player
                        val world = player.world
                        val period = StringArgumentType.getString(context, "period")

                        when (period.lowercase()) {
                            "day" -> {
                                world.time = 1000

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("time.day"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("☀ ", NamedTextColor.YELLOW))
                                    .append(Component.text("Day", NamedTextColor.GOLD, TextDecoration.BOLD))
                                    .append(Component.text(" ☀", NamedTextColor.YELLOW))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            "noon" -> {
                                world.time = 6000

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.8f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("time.noon"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("☀ ", NamedTextColor.GOLD))
                                    .append(Component.text("Noon", NamedTextColor.YELLOW, TextDecoration.BOLD))
                                    .append(Component.text(" ☀", NamedTextColor.GOLD))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            "night" -> {
                                world.time = 13000

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("time.night"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("★ ", NamedTextColor.DARK_BLUE))
                                    .append(Component.text("Night", NamedTextColor.BLUE, TextDecoration.BOLD))
                                    .append(Component.text(" ★", NamedTextColor.DARK_BLUE))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            "midnight" -> {
                                world.time = 18000

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("time.midnight"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("☾ ", NamedTextColor.DARK_GRAY))
                                    .append(Component.text("Midnight", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                                    .append(Component.text(" ☾", NamedTextColor.DARK_GRAY))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            else -> return@executes 0
                        }

                        1
                    }
            )
            .build()
    }

    override val description: String
        get() = "Change time"
}