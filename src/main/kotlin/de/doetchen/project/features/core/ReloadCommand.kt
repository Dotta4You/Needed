package de.doetchen.project.features.core

import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReloadCommand(private val plugin: Needed) : CommandBuilder {

    override val description = "Reload the plugin configuration"
    override val aliases = emptyList<String>()

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("needed")
            .then(
                Commands.literal("reload")
                    .requires { it.sender.hasPermission("needed.reload") }
                    .executes { context ->
                        val sender = context.source.sender
                        performReload(sender)
                        1
                    }
            )
            .build()
    }

    private fun performReload(sender: CommandSender) {
        val startTime = System.currentTimeMillis()

        val startMessage = plugin.languageManager.getPrefix()
            .append(Component.text(" "))
            .append(plugin.languageManager.getMessage("reload.starting"))
        sender.sendMessage(startMessage)

        try {
            plugin.reloadConfig()
            sender.sendMessage(
                Component.text("  ")
                    .append(plugin.languageManager.getMessage("reload.config"))
            )

            plugin.languageManager.reload()
            sender.sendMessage(
                Component.text("  ")
                    .append(plugin.languageManager.getMessage("reload.language"))
            )

            plugin.moduleManager.reload()
            sender.sendMessage(
                Component.text("  ")
                    .append(plugin.languageManager.getMessage("reload.modules"))
            )

            plugin.conversationManager.clear()
            sender.sendMessage(
                Component.text("  ")
                    .append(plugin.languageManager.getMessage("reload.conversations"))
            )

            val duration = System.currentTimeMillis() - startTime

            val successMessage = plugin.languageManager.getPrefix()
                .append(Component.text(" "))
                .append(plugin.languageManager.getMessage("reload.success", "time" to duration.toString()))
            sender.sendMessage(successMessage)

            if (sender is Player && plugin.config.getBoolean("settings.sounds.enabled", true)) {
                sender.playSound(sender.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f)
            }
            plugin.logger.info("Plugin reloaded by ${sender.name} in ${duration}ms")

        } catch (e: Exception) {
            val errorMessage = plugin.languageManager.getPrefix()
                .append(Component.text(" "))
                .append(plugin.languageManager.getMessage("reload.error", "error" to (e.message ?: "Unknown error")))
            sender.sendMessage(errorMessage)

            plugin.logger.severe("Error during reload: ${e.message}")
            e.printStackTrace()
        }
    }
}

