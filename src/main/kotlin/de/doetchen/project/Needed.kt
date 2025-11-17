package de.doetchen.project

import de.doetchen.project.commands.GameModeCommand
import de.doetchen.project.commands.TimeCommand
import de.doetchen.project.commands.WeatherCommand
import de.doetchen.project.manager.LanguageManager
import de.doetchen.project.manager.ModuleManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Needed : JavaPlugin() {

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var languageManager: LanguageManager
        private set

    override fun onEnable() {
        saveDefaultConfig()
        moduleManager = ModuleManager(this)
        languageManager = LanguageManager(this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands = event.registrar()

            if (moduleManager.isModuleEnabled("gamemode")) {
                val gmCommand = GameModeCommand(this)
                commands.register(gmCommand.register(), gmCommand.description, gmCommand.aliases)
            }

            if (moduleManager.isModuleEnabled("weather")) {
                val weatherCommand = WeatherCommand(this)
                commands.register(weatherCommand.register(), weatherCommand.description)
            }

            if (moduleManager.isModuleEnabled("time")) {
                val timeCommand = TimeCommand(this)
                commands.register(timeCommand.register(), timeCommand.description)
            }
        }
        logger.info("Needed loaded!")
    }

    override fun onDisable() {
        logger.info("Needed unloaded!")
    }
}
