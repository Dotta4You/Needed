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

class ClearInventoryCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("clear")
            .requires { it.sender is Player && it.sender.hasPermission("needed.clear") }
            .executes { context ->
                val player = context.source.sender as Player
                clearInventory(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.clear.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) clearInventory(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun clearInventory(sender: Player, target: Player) {
        target.inventory.clear()

        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.ENTITY_ITEM_PICKUP, 0.5f, 0.5f)
        }

        val message = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.clear.cleared"))
        target.sendMessage(message)

        if (sender != target) {
            val senderMessage = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage(
                    "moderation.clear.cleared-other",
                    "player" to target.name
                ))
            sender.sendMessage(senderMessage)
        }
    }

    override val aliases = listOf("ci")
    override val description = "Clear player inventory"
}

