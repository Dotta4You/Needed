package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.entity.Player

class TeleportAllCommand(private val plugin: Needed) : CommandBuilder {

    override val description = "Teleport all players to a location"
    override val aliases = emptyList<String>()

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("tpall")
            .requires { it.sender is Player && it.sender.hasPermission("needed.tpall") }
            .executes { context ->
                val player = context.source.sender as Player
                teleportAllToPlayer(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()

                        if (target != null) {
                            teleportAllToPlayer(sender, target)
                        }
                        1
                    }
            )
            .build()
    }

    private fun teleportAllToPlayer(sender: Player, target: Player) {
        val location = target.location
        var teleportedCount = 0

        plugin.server.onlinePlayers.forEach { player ->
            if (player != target) {
                player.teleport(location)
                teleportedCount++

                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                    player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f)
                }

                val teleportMessage = plugin.languageManager.getPrefix()
                    .append(Component.text(" You have been teleported to ", NamedTextColor.GREEN))
                    .append(Component.text(target.name, NamedTextColor.GOLD, TextDecoration.BOLD))
                player.sendMessage(teleportMessage)
            }
        }

        val senderMessage = plugin.languageManager.getPrefix()
            .append(Component.text(" Teleported ", NamedTextColor.GREEN))
            .append(Component.text("$teleportedCount", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text(" players to ", NamedTextColor.GREEN))
            .append(Component.text(target.name, NamedTextColor.GOLD, TextDecoration.BOLD))
        sender.sendMessage(senderMessage)

        if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
            sender.playSound(sender.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f)
        }
    }
}

