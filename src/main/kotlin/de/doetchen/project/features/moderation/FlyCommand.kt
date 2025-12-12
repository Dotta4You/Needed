package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.Sound
import org.bukkit.entity.Player

class FlyCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("fly")
            .requires { it.sender is Player && it.sender.hasPermission("needed.fly") }
            .executes { context ->
                val player = context.source.sender as Player
                toggleFly(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.fly.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) toggleFly(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun toggleFly(sender: Player, target: Player) {
        val moduleConfig = plugin.moduleManager.getModuleConfig("fly")
        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
        val showActionBar = moduleConfig?.getBoolean("settings.show-actionbar", true) ?: true
        val defaultSpeed = moduleConfig?.getDouble("settings.default-speed", 0.1) ?: 0.1

        target.allowFlight = !target.allowFlight

        if (target.allowFlight) {
            target.isFlying = true
            target.flySpeed = defaultSpeed.toFloat()
        } else {
            target.isFlying = false
        }

        if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
            val sound = if (target.allowFlight) Sound.ENTITY_BAT_TAKEOFF else Sound.ENTITY_BAT_DEATH
            target.playSound(target.location, sound, 0.5f, 1.0f)
        }

        val status = if (target.allowFlight) "enabled" else "disabled"
        val message = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.fly.$status"))
        target.sendMessage(message)

        if (showActionBar) {
            val actionBarText = plugin.languageManager.getMessage("moderation.fly.actionbar.$status")
            val actionBar = if (target.allowFlight) {
                net.kyori.adventure.text.Component.text("🕊 ", net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .append(actionBarText.color(net.kyori.adventure.text.format.NamedTextColor.AQUA).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD))
                    .append(net.kyori.adventure.text.Component.text(" 🕊", net.kyori.adventure.text.format.NamedTextColor.AQUA))
            } else {
                net.kyori.adventure.text.Component.text("✗ ", net.kyori.adventure.text.format.NamedTextColor.RED)
                    .append(actionBarText.color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
                    .append(net.kyori.adventure.text.Component.text(" ✗", net.kyori.adventure.text.format.NamedTextColor.RED))
            }
            target.sendActionBar(actionBar)
        }

        if (sender != target) {
            val senderMessage = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage(
                    "moderation.fly.$status-other",
                    "player" to target.name
                ))
            sender.sendMessage(senderMessage)
        }
    }

    override val description = "Toggle fly mode"
}

