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

class InvseeCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("invsee")
            .requires { it.sender is Player && it.sender.hasPermission("needed.invsee") }
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
                            if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                player.playSound(player.location, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f)
                            }

                            player.openInventory(target.inventory)

                            val message = plugin.languageManager.getPrefix()
                                .append(plugin.languageManager.getMessage(
                                    "moderation.invsee.opened",
                                    "player" to target.name
                                ))
                            player.sendMessage(message)
                        }

                        1
                    }
            )
            .build()
    }

    override val description = "View player's inventory"
}

