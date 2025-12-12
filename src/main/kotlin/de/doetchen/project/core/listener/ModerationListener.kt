package de.doetchen.project.core.listener

import de.doetchen.project.Needed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class ModerationListener(private val plugin: Needed) : Listener {

    // Note: Advanced freeze/vanish/god functionality would require
    // storing command instances or using a centralized state manager
    // For now, this is a placeholder for future event-based features

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerMove(event: PlayerMoveEvent) {
        // Movement prevention handled via potion effects in FreezeCommand
        // Additional logic can be added here if needed
    }
}

