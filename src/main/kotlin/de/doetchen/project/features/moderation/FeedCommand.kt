package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Sound
import org.bukkit.entity.Player

class FeedCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("feed")
            .requires { it.sender is Player && it.sender.hasPermission("needed.feed") }
            .executes { context ->
                val player = context.source.sender as Player
                feed(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.feed.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) feed(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun feed(sender: Player, target: Player) {
        target.foodLevel = 20
        target.saturation = 20f
        target.exhaustion = 0f

        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f)
        }

        val message = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.feed.fed"))
        target.sendMessage(message)

        if (sender != target) {
            val senderMessage = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage(
                    "moderation.feed.fed-other",
                    "player" to target.name
                ))
            sender.sendMessage(senderMessage)
        }
    }

    override val aliases = listOf("eat")
    override val description = "Feed player"
}

