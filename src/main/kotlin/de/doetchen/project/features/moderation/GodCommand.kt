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
import java.util.UUID

class GodCommand(private val plugin: Needed) : CommandBuilder {

    private val godPlayers = mutableSetOf<UUID>()

    override val description = "Toggle god mode"

    fun hasGodMode(player: Player): Boolean = godPlayers.contains(player.uniqueId)

    @Suppress("UNUSED")
    fun hasGod(player: Player): Boolean = godPlayers.contains(player.uniqueId)

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("god")
            .requires { it.sender is Player && it.sender.hasPermission("needed.god") }
            .executes { context ->
                val player = context.source.sender as Player
                toggleGod(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.god.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) toggleGod(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun toggleGod(sender: Player, target: Player) {
        val moduleConfig = plugin.moduleManager.getModuleConfig("god")
        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
        val showActionBar = moduleConfig?.getBoolean("settings.show-actionbar", true) ?: true
        val preventHunger = moduleConfig?.getBoolean("settings.prevent-hunger", true) ?: true

        if (godPlayers.contains(target.uniqueId)) {
            godPlayers.remove(target.uniqueId)
            target.isInvulnerable = false

            if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                target.playSound(target.location, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.5f, 0.8f)
            }

            val message = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage("moderation.god.disabled"))
            target.sendMessage(message)

            if (showActionBar) {
                val actionBar = Component.text()
                    .append(Component.text("✗ ", NamedTextColor.RED))
                    .append(Component.text("God Mode", NamedTextColor.GRAY))
                    .append(Component.text(" ✗", NamedTextColor.RED))
                    .build()
                target.sendActionBar(actionBar)
            }

            if (sender != target) {
                val senderMessage = plugin.languageManager.getPrefix()
                    .append(plugin.languageManager.getMessage(
                        "moderation.god.disabled-other",
                        "player" to target.name
                    ))
                sender.sendMessage(senderMessage)
            }
        } else {
            godPlayers.add(target.uniqueId)
            target.isInvulnerable = true

            if (preventHunger) {
                target.foodLevel = 20
                target.saturation = 20f
            }

            if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                target.playSound(target.location, Sound.ENTITY_WITHER_SPAWN, 0.3f, 2.0f)
            }

            val message = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage("moderation.god.enabled"))
            target.sendMessage(message)

            if (showActionBar) {
                val actionBarText = plugin.languageManager.getMessage("moderation.god.actionbar.enabled")
                val actionBar = Component.text()
                    .append(Component.text("✓ ", NamedTextColor.GOLD))
                    .append(actionBarText.color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                    .append(Component.text(" ✓", NamedTextColor.GOLD))
                    .build()
                target.sendActionBar(actionBar)
            }

            if (sender != target) {
                val senderMessage = plugin.languageManager.getPrefix()
                    .append(plugin.languageManager.getMessage(
                        "moderation.god.enabled-other",
                        "player" to target.name
                    ))
                sender.sendMessage(senderMessage)
            }
        }
    }
}

