package de.doetchen.project.features.messaging

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

class MessageCommand(private val plugin: Needed) : CommandBuilder {

    override val aliases = listOf("msg", "tell", "whisper", "w")
    override val description = "Send a private message to a player"

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("message")
            .requires { it.sender is Player }
            .then(
                Commands.argument("player", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                            .executes { context ->
                                val sender = context.source.sender as Player
                                val target = context.getArgument("player",
                                    io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                                ).resolve(context.source).firstOrNull()
                                val message = StringArgumentType.getString(context, "message")

                                if (target == null) {
                                    val errorMessage = plugin.languageManager.getPrefix()
                                        .append(plugin.languageManager.getMessage("message.player-not-found"))
                                    sender.sendMessage(errorMessage)
                                    return@executes 0
                                }

                                if (sender.uniqueId == target.uniqueId) {
                                    val errorMessage = plugin.languageManager.getPrefix()
                                        .append(plugin.languageManager.getMessage("message.cannot-message-yourself"))
                                    sender.sendMessage(errorMessage)
                                    return@executes 0
                                }

                                val miniMessage = MiniMessage.miniMessage()
                                val formattedMessage = miniMessage.deserialize(message)

                                val senderFormat = plugin.languageManager.getMessage("message.format-sender")
                                    .replaceText { it.matchLiteral("{player}").replacement(target.name()) }
                                    .replaceText { it.matchLiteral("{message}").replacement(formattedMessage) }

                                val receiverFormat = plugin.languageManager.getMessage("message.format-receiver")
                                    .replaceText { it.matchLiteral("{player}").replacement(sender.name()) }
                                    .replaceText { it.matchLiteral("{message}").replacement(formattedMessage) }

                                sender.sendMessage(senderFormat)
                                target.sendMessage(receiverFormat)

                                plugin.conversationManager.setLastConversation(sender.uniqueId, target.uniqueId)
                                plugin.conversationManager.setLastConversation(target.uniqueId, sender.uniqueId)

                                1
                            }
                    )
            )
            .build()
    }
}

