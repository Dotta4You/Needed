package de.doetchen.project.features.messaging

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ReplyCommand(private val plugin: Needed) : CommandBuilder {

    override val aliases = listOf("r")
    override val description = "Reply to the last private message"

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("reply")
            .requires { it.sender is Player }
            .then(
                Commands.argument("message", StringArgumentType.greedyString())
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val message = StringArgumentType.getString(context, "message")

                        if (!plugin.conversationManager.hasConversation(sender.uniqueId)) {
                            val errorMessage = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage("message.no-conversation"))
                            sender.sendMessage(errorMessage)
                            return@executes 0
                        }

                        val targetUuid = plugin.conversationManager.getLastConversation(sender.uniqueId)!!
                        val target = Bukkit.getPlayer(targetUuid)

                        if (target == null || !target.isOnline) {
                            val errorMessage = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage("message.player-offline"))
                            sender.sendMessage(errorMessage)
                            plugin.conversationManager.clearConversation(sender.uniqueId)
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
            .build()
    }
}

