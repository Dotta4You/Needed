package de.doetchen.project

import de.doetchen.project.commands.registerCommands
import de.doetchen.project.manager.CommandRegistry
import de.doetchen.project.manager.LanguageManager
import de.doetchen.project.manager.ModuleManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Needed : JavaPlugin() {

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var languageManager: LanguageManager
        private set

    private lateinit var commandRegistry: CommandRegistry

    override fun onEnable() {
        saveDefaultConfig()
        moduleManager = ModuleManager(this)
        languageManager = LanguageManager(this)
        commandRegistry = CommandRegistry(this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        commandRegistry.registerCommands()

        logger.info("Needed loaded!")
    }


        logger.info("Needed unloaded!")
    }
}
