package de.doetchen.project.features.moderation

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player

class TeleportCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tp")
            .requires { it.sender is Player && it.sender.hasPermission("needed.tp") }
            .executes { context ->
                val player = context.source.sender as Player
                val usageMessage = plugin.languageManager.getPrefix()
                    .append(Component.text(" Usage:", NamedTextColor.RED))
                player.sendMessage(usageMessage)
                player.sendMessage(Component.text("  /tp <player>", NamedTextColor.GRAY))
                player.sendMessage(Component.text("  /tp <player> <destination>", NamedTextColor.GRAY))
                player.sendMessage(Component.text("  /tp <x> <y> <z>", NamedTextColor.GRAY))
                1
            }
            .then(
                // /tp <x> <y> <z> - Coordinates
                Commands.argument("x", DoubleArgumentType.doubleArg())
                    .then(
                        Commands.argument("y", DoubleArgumentType.doubleArg())
                            .then(
                                Commands.argument("z", DoubleArgumentType.doubleArg())
                                    .executes { context ->
                                        val player = context.source.sender as Player
                                        val x = DoubleArgumentType.getDouble(context, "x")
                                        val y = DoubleArgumentType.getDouble(context, "y")
                                        val z = DoubleArgumentType.getDouble(context, "z")

                                        val location = Location(player.world, x, y, z)
                                        player.teleport(location)

                                        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f)
                                        }

                                        val message = plugin.languageManager.getPrefix()
                                            .append(Component.text(" Teleported to ", NamedTextColor.GREEN))
                                            .append(Component.text("$x, $y, $z", NamedTextColor.GOLD))
                                        player.sendMessage(message)
                                        1
                                    }
                            )
                    )
            )
            .then(
                // /tp <player> - Teleport to player
                Commands.argument("target", ArgumentTypes.player())
                    .executes { context ->
                        val player = context.source.sender as Player
                        val target = context.getArgument("target",
                            PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()

                        if (target != null) {
                            player.teleport(target.location)

                            if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f)
                            }

                            val message = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage(
                                    "moderation.tp.teleported",
                                    "player" to target.name
                                ))
                            player.sendMessage(message)
                        }

                        1
                    }
                    .then(
                        Commands.argument("destination", ArgumentTypes.player())
                            .suggests { _, builder ->
                                plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                                builder.buildFuture()
                            }
                            .requires { it.sender.hasPermission("needed.tp.other") }
                            .executes { context ->
                                val sender = context.source.sender as Player
                                val target = context.getArgument("target",
                                    io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                                ).resolve(context.source).firstOrNull()
                                val destination = context.getArgument("destination",
                                    io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                                ).resolve(context.source).firstOrNull()

                                if (target != null && destination != null) {
                                    target.teleport(destination.location)

                                    if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                        target.playSound(target.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f)
                                        sender.playSound(sender.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.3f, 1.0f)
                                    }

                                    val targetMessage = plugin.languageManager.getPrefix()
                                        .append(plugin.languageManager.getMessage(
                                            "moderation.tp.teleported",
                                            "player" to destination.name
                                        ))
                                    target.sendMessage(targetMessage)

                                    val senderMessage = plugin.languageManager.getPrefix()
                                        .append(plugin.languageManager.getMessage(
                                            "moderation.tp.teleported-other",
                                            "player" to target.name,
                                            "destination" to destination.name
                                        ))
                                    sender.sendMessage(senderMessage)
                                }

                                1
                            }
                    )
            )
            .build()
    }

    override val aliases = listOf("teleport")
    override val description = "Teleport to player"
}

