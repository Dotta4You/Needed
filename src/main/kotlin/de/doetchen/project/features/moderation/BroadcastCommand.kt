package de.doetchen.project.features.moderation

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound

class BroadcastCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("broadcast")
            .requires { it.sender.hasPermission("needed.broadcast") }
            .executes { context ->
                val sender = context.source.sender
                val usageMessage = net.kyori.adventure.text.Component.text("[", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                    .append(net.kyori.adventure.text.Component.text("Needed", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    .append(net.kyori.adventure.text.Component.text("] ", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY))
                    .append(net.kyori.adventure.text.Component.text("Usage: ", net.kyori.adventure.text.format.NamedTextColor.RED))
                    .append(net.kyori.adventure.text.Component.text("/broadcast <message>", net.kyori.adventure.text.format.NamedTextColor.GRAY))
                sender.sendMessage(usageMessage)
                1
            }
            .then(
                Commands.argument("message", StringArgumentType.greedyString())
                    .executes { context ->
                        val sender = context.source.sender
                        val message = StringArgumentType.getString(context, "message")

                        val moduleConfig = plugin.moduleManager.getModuleConfig("broadcast")
                        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
                        val useCustomPrefix = moduleConfig?.getBoolean("settings.use-custom-prefix", true) ?: true
                        val customPrefix = moduleConfig?.getString("settings.custom-prefix", "<gradient:#ff0000:#ffff00>BROADCAST</gradient>")
                            ?: "<gradient:#ff0000:#ffff00>BROADCAST</gradient>"
                        val soundType = try {
                            Sound.valueOf(moduleConfig?.getString("settings.broadcast-sound", "ENTITY_PLAYER_LEVELUP") ?: "ENTITY_PLAYER_LEVELUP")
                        } catch (e: IllegalArgumentException) {
                            Sound.ENTITY_PLAYER_LEVELUP
                        }

                        val broadcastMessage = if (useCustomPrefix) {
                            val miniMessage = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                            Component.text()
                                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                                .append(miniMessage.deserialize(customPrefix))
                                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(message, NamedTextColor.YELLOW))
                                .build()
                        } else {
                            Component.text()
                                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                                .append(Component.text("Broadcast", NamedTextColor.RED, TextDecoration.BOLD))
                                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(message, NamedTextColor.YELLOW))
                                .build()
                        }

                        plugin.server.onlinePlayers.forEach { player ->
                            player.sendMessage(broadcastMessage)
                            if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                player.playSound(player.location, soundType, 0.5f, 1.0f)
                            }
                        }

                        plugin.server.consoleSender.sendMessage(broadcastMessage)

                        val confirmMessage = plugin.languageManager.getPrefix()
                            .append(plugin.languageManager.getMessage("moderation.broadcast.sent"))
                        sender.sendMessage(confirmMessage)

                        1
                    }
            )
            .build()
    }

    override val aliases = listOf("bc", "announce")
    override val description = "Broadcast message to all players"
}

