package de.doetchen.project.features.moderation

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.Sound
import org.bukkit.entity.Player

class SpeedCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("speed")
            .requires { it.sender is Player && it.sender.hasPermission("needed.speed") }
            .then(
                Commands.argument("amount", FloatArgumentType.floatArg(0f, 10f))
                    .executes { context ->
                        val player = context.source.sender as Player
                        val speed = FloatArgumentType.getFloat(context, "amount")
                        setSpeed(player, player, speed)
                        1
                    }
                    .then(
                        Commands.argument("target", ArgumentTypes.player())
                            .suggests { _, builder ->
                                plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                                builder.buildFuture()
                            }
                            .requires { it.sender.hasPermission("needed.speed.other") }
                            .executes { context ->
                                val sender = context.source.sender as Player
                                val target = context.getArgument("target",
                                    PlayerSelectorArgumentResolver::class.java
                                ).resolve(context.source).firstOrNull()
                                val speed = FloatArgumentType.getFloat(context, "amount")
                                if (target != null) setSpeed(sender, target, speed)
                                1
                            }
                    )
            )
            .build()
    }

    private fun setSpeed(sender: Player, target: Player, speed: Float) {
        val normalizedSpeed = speed / 10f

        if (target.isFlying || target.allowFlight) {
            target.flySpeed = normalizedSpeed
        } else {
            target.walkSpeed = normalizedSpeed
        }

        // Determine if walking or flying
        val speedType = if (target.isFlying || target.allowFlight) "fly" else "walk"

        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.ENTITY_HORSE_GALLOP, 0.5f, 1.0f + speed / 10f)
        }

        val message = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage(
                "moderation.speed.changed",
                "speed" to speed.toString(),
                "type" to speedType
            ))
        target.sendMessage(message)

        if (sender != target) {
            val senderMessage = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage(
                    "moderation.speed.changed-other",
                    "player" to target.name,
                    "speed" to speed.toString()
                ))
            sender.sendMessage(senderMessage)
        }
    }

    override val description = "Change movement speed"
}

