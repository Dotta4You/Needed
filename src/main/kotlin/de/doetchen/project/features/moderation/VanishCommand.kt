package de.doetchen.project.features.moderation

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.UUID

class VanishCommand(private val plugin: Needed) : CommandBuilder {

    private val vanishedPlayers = mutableSetOf<UUID>()

    override val description = "Toggle vanish mode"
    override val aliases = listOf("v")

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("vanish")
            .requires { it.sender is Player && it.sender.hasPermission("needed.vanish") }
            .executes { context ->
                val player = context.source.sender as Player
                toggleVanish(player, player)
                1
            }
            .then(
                Commands.argument("target", ArgumentTypes.player())
                    .suggests { _, builder ->
                        plugin.server.onlinePlayers.forEach { builder.suggest(it.name) }
                        builder.buildFuture()
                    }
                    .requires { it.sender.hasPermission("needed.vanish.other") }
                    .executes { context ->
                        val sender = context.source.sender as Player
                        val target = context.getArgument("target",
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()
                        if (target != null) toggleVanish(sender, target)
                        1
                    }
            )
            .build()
    }

    private fun toggleVanish(sender: Player, target: Player) {
        val moduleConfig = plugin.moduleManager.getModuleConfig("vanish")
        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
        val showActionBar = moduleConfig?.getBoolean("settings.show-actionbar", true) ?: true

        if (vanishedPlayers.contains(target.uniqueId)) {
            vanishedPlayers.remove(target.uniqueId)
            plugin.server.onlinePlayers.forEach { it.showPlayer(plugin, target) }

            if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                target.playSound(target.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.8f)
            }

            val message = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage("moderation.vanish.disabled"))
            target.sendMessage(message)

            if (showActionBar) {
                val actionBar = Component.text()
                    .append(Component.text("👁 ", NamedTextColor.GREEN))
                    .append(Component.text("Sichtbar", NamedTextColor.GREEN, TextDecoration.BOLD))
                    .append(Component.text(" 👁", NamedTextColor.GREEN))
                    .build()
                target.sendActionBar(actionBar)
            }

            if (sender != target) {
                val senderMessage = plugin.languageManager.getPrefix()
                    .append(plugin.languageManager.getMessage(
                        "moderation.vanish.disabled-other",
                        "player" to target.name
                    ))
                sender.sendMessage(senderMessage)
            }
        } else {
            vanishedPlayers.add(target.uniqueId)

            plugin.server.onlinePlayers.forEach {
                if (!it.hasPermission("needed.vanish.see")) {
                    it.hidePlayer(plugin, target)
                }
            }

            if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                target.playSound(target.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f)
            }

            val message = plugin.languageManager.getPrefix()
                .append(plugin.languageManager.getMessage("moderation.vanish.enabled"))
            target.sendMessage(message)

            if (showActionBar) {
                val actionBarText = plugin.languageManager.getMessage("moderation.vanish.actionbar.enabled")
                val actionBar = Component.text()
                    .append(Component.text("👻 ", NamedTextColor.GRAY))
                    .append(actionBarText.color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
                    .append(Component.text(" 👻", NamedTextColor.GRAY))
                    .build()
                target.sendActionBar(actionBar)
            }

            if (sender != target) {
                val senderMessage = plugin.languageManager.getPrefix()
                    .append(plugin.languageManager.getMessage(
                        "moderation.vanish.enabled-other",
                        "player" to target.name
                    ))
                sender.sendMessage(senderMessage)
            }
        }
    }
}

