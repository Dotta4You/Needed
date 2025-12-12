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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

class FreezeCommand(private val plugin: Needed) : CommandBuilder {

    private val frozenPlayers = mutableSetOf<UUID>()

    override val description = "Freeze a player"

    fun isFrozen(player: Player): Boolean = frozenPlayers.contains(player.uniqueId)

    fun cleanup(player: Player) {
        frozenPlayers.remove(player.uniqueId)
        player.removePotionEffect(PotionEffectType.SLOWNESS)
        player.removePotionEffect(PotionEffectType.JUMP_BOOST)
    }

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("freeze")
            .requires { it.sender is Player && it.sender.hasPermission("needed.freeze") }
            .executes { context ->
                val player = context.source.sender as Player
                val usageMessage = plugin.languageManager.getPrefix()
                    .append(Component.text(" Usage: ", net.kyori.adventure.text.format.NamedTextColor.RED))
                    .append(Component.text("/freeze <player>", net.kyori.adventure.text.format.NamedTextColor.GRAY))
                player.sendMessage(usageMessage)
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
                            io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java
                        ).resolve(context.source).firstOrNull()

                        if (target != null) {
                            if (frozenPlayers.contains(target.uniqueId)) {
                                unfreeze(sender, target)
                            } else {
                                freeze(sender, target)
                            }
                        }
                        1
                    }
            )
            .build()
    }

    private fun freeze(sender: Player, target: Player) {
        val moduleConfig = plugin.moduleManager.getModuleConfig("freeze")
        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
        val showActionBar = moduleConfig?.getBoolean("settings.show-actionbar", true) ?: true

        frozenPlayers.add(target.uniqueId)

        target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 255, false, false, false))
        target.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, Int.MAX_VALUE, 200, false, false, false))

        if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f)
            sender.playSound(sender.location, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f)
        }

        val targetMessage = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.freeze.frozen"))
        target.sendMessage(targetMessage)

        if (showActionBar) {
            val actionBar = Component.text("❄ ", NamedTextColor.AQUA)
                .append(Component.text("EINGEFROREN", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" ❄", NamedTextColor.AQUA))
            target.sendActionBar(actionBar)
        }

        val senderMessage = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage(
                "moderation.freeze.enabled",
                "player" to target.name
            ))
        sender.sendMessage(senderMessage)
    }

    private fun unfreeze(sender: Player, target: Player) {
        frozenPlayers.remove(target.uniqueId)

        target.removePotionEffect(PotionEffectType.SLOWNESS)
        target.removePotionEffect(PotionEffectType.JUMP_BOOST)

        val moduleConfig = plugin.moduleManager.getModuleConfig("freeze")
        val playSound = moduleConfig?.getBoolean("settings.play-sound", true) ?: true
        val showActionBar = moduleConfig?.getBoolean("settings.show-actionbar", true) ?: true

        if (playSound && plugin.config.getBoolean("settings.sounds.enabled", true)) {
            target.playSound(target.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f)
            sender.playSound(sender.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
        }

        val targetMessage = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage("moderation.freeze.unfrozen"))
        target.sendMessage(targetMessage)

        if (showActionBar) {
            val actionBarText = plugin.languageManager.getMessage("moderation.freeze.actionbar.unfrozen")
            val actionBar = Component.text("✓ ", NamedTextColor.GREEN)
                .append(actionBarText.color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
                .append(Component.text(" ✓", NamedTextColor.GREEN))
            target.sendActionBar(actionBar)
        }

        val senderMessage = plugin.languageManager.getPrefix()
            .append(plugin.languageManager.getMessage(
                "moderation.freeze.disabled",
                "player" to target.name
            ))
        sender.sendMessage(senderMessage)
    }
}

