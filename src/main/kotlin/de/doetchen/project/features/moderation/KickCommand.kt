package de.doetchen.project.features.moderation

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class KickCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("kick")
            .requires { it.sender.hasPermission("needed.kick") }
            .executes { context ->
                val sender = context.source.sender
                val usageMessage = net.kyori.adventure.text.Component.text("[", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY)
                    .append(net.kyori.adventure.text.Component.text("Needed", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                    .append(net.kyori.adventure.text.Component.text("] ", net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY))
                    .append(net.kyori.adventure.text.Component.text("Usage: ", net.kyori.adventure.text.format.NamedTextColor.RED))
                    .append(net.kyori.adventure.text.Component.text("/kick <player> [reason]", net.kyori.adventure.text.format.NamedTextColor.GRAY))
                sender.sendMessage(usageMessage)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val sender = context.source.sender
                        val target = context.getArgument("target",
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()

                        if (target != null) {
                            val reason = Component.text("Kicked from server", NamedTextColor.RED)
                            target.kick(reason)

                            val message = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage(
                                    "moderation.kick.kicked",
                                    "player" to target.name
                                ))
                            sender.sendMessage(message)
                        }

                        1
                    }
                    .then(
                        Commands.argument("reason", StringArgumentType.greedyString())
                            .executes { context ->
                                val sender = context.source.sender
                                val target = context.getArgument("target",
                                    io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                                ).resolve(context.source).firstOrNull()
                                val reason = StringArgumentType.getString(context, "reason")

                                if (target != null) {
                                    val kickMessage = Component.text()
                                        .append(Component.text("Du wurdest gekickt!", NamedTextColor.RED))
                                        .appendNewline()
                                        .append(Component.text("Grund: ", NamedTextColor.GRAY))
                                        .append(Component.text(reason, NamedTextColor.WHITE))
                                        .build()

                                    target.kick(kickMessage)

                                    val message = plugin.languageManager.getPrefix()
                                        .append(plugin.languageManager.getMessage(
                                            "moderation.kick.kicked-reason",
                                            "player" to target.name,
                                            "reason" to reason
                                        ))
                                    sender.sendMessage(message)
                                }

                                1
                            }
                    )
            )
            .build()
    }

    override val description = "Kick player from server"
}

