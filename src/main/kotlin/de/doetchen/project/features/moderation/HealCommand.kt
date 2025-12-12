package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player

class HealCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("heal")
            .requires { it.sender is Player && it.sender.hasPermission("needed.heal") }
            .executes { context ->
                val player = context.source.sender as Player
                heal(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.heal.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) heal(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun heal(sender: Player, target: Player) {
        val maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        target.health = maxHealth
        target.foodLevel = 20
        target.saturation = 20f
        target.fireTicks = 0
        target.activePotionEffects.forEach { target.removePotionEffect(it.type) }

        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
        }

        val message = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.heal.healed"))
        target.sendMessage(message)

        if (sender != target) {
            val senderMessage = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage(
                    "moderation.heal.healed-other",
                    "player" to target.name
                ))
            sender.sendMessage(senderMessage)
        }
    }

    override val description = "Heal player"
}

