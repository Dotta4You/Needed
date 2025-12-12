package de.doetchen.project

import de.doetchen.project.core.extensions.CommandBuilder
import de.doetchen.project.core.manager.CommandRegistry
import de.doetchen.project.core.manager.ConversationManager
import de.doetchen.project.core.manager.LanguageManager
import de.doetchen.project.core.manager.ModuleManager
import de.doetchen.project.features.basic.GameModeCommand
import de.doetchen.project.features.basic.TimeCommand
import de.doetchen.project.features.basic.WeatherCommand
import de.doetchen.project.features.core.HelpCommand
import de.doetchen.project.features.messaging.MessageCommand
import de.doetchen.project.features.messaging.ReplyCommand
import de.doetchen.project.metrics.MetricsHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Needed : JavaPlugin() {

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var languageManager: LanguageManager
        private set

    lateinit var conversationManager: ConversationManager
        private set


    private lateinit var commandRegistry: CommandRegistry
    private lateinit var metricsHandler: MetricsHandler

    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()

        moduleManager = ModuleManager(this)
        languageManager = LanguageManager(this)
        conversationManager = ConversationManager()
        commandRegistry = CommandRegistry(this)

        server.pluginManager.registerEvents(de.doetchen.project.core.listener.ModerationListener(this), this)

        metricsHandler = MetricsHandler(this)
        metricsHandler.initialize()

        registerCommands()

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            commandRegistry.registerAll(event)
        }

        displayModuleStatus()

        logger.info("Needed loaded successfully!")
    }

    override fun onDisable() {
        if (::metricsHandler.isInitialized) {
            metricsHandler.shutdown()
        }
        conversationManager.clear()
        logger.info("Needed unloaded!")
    }
    private fun registerCommands() {
        // Core Commands
        command { HelpCommand(it) }
        command { de.doetchen.project.features.core.ReloadCommand(it) }

        // Basic Commands
        command(module = "basic.gamemode") { GameModeCommand(it) }
        command(module = "basic.weather") { WeatherCommand(it) }
        command(module = "basic.time") { TimeCommand(it) }

        // Messaging Commands
        if (moduleManager.isModuleEnabled("messaging")) {
            command { MessageCommand(it) }
            command { ReplyCommand(it) }
        }

        // Moderation Commands
        command(module = "moderation.vanish") { de.doetchen.project.features.moderation.VanishCommand(it) }
        command(module = "moderation.freeze") { de.doetchen.project.features.moderation.FreezeCommand(it) }
        command(module = "moderation.fly") { de.doetchen.project.features.moderation.FlyCommand(it) }
        command(module = "moderation.god") { de.doetchen.project.features.moderation.GodCommand(it) }
        command(module = "moderation.speed") { de.doetchen.project.features.moderation.SpeedCommand(it) }
        command(module = "moderation.heal") { de.doetchen.project.features.moderation.HealCommand(it) }
        command(module = "moderation.feed") { de.doetchen.project.features.moderation.FeedCommand(it) }
        command(module = "moderation.invsee") { de.doetchen.project.features.moderation.InvseeCommand(it) }
        command(module = "moderation.clear") { de.doetchen.project.features.moderation.ClearInventoryCommand(it) }
        command(module = "moderation.kick") { de.doetchen.project.features.moderation.KickCommand(it) }
        command(module = "moderation.broadcast") { de.doetchen.project.features.moderation.BroadcastCommand(it) }
        command(module = "moderation.teleport") { de.doetchen.project.features.moderation.TeleportCommand(it) }
        command(module = "moderation.teleporthere") { de.doetchen.project.features.moderation.TeleportHereCommand(it) }
        command(module = "moderation.tpall") { de.doetchen.project.features.moderation.TeleportAllCommand(it) }
    }

    private fun command(module: String = "", builder: (Needed) -> CommandBuilder) {
        commandRegistry.command(module, builder)
    }

    private fun displayModuleStatus() {
        val allModules = moduleManager.getAllModules()
        val enabledCount = allModules.count { it.enabled }
        val disabledCount = allModules.size - enabledCount

        logger.info("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        logger.info("§a§lModules §8(§a$enabledCount enabled §8| §c$disabledCount disabled§8)")
        logger.info("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Group Modules (Categorical)
        val modulesByCategory = allModules.groupBy { it.category }

        modulesByCategory.forEach { (category, modules) ->
            logger.info("  §7$category§8:")
            modules.forEach { module ->
                val status = if (module.enabled) "§a✓" else "§c✗"
                val color = if (module.enabled) "§a" else "§c"
                logger.info("    $status $color${module.name}")
            }
        }

        logger.info("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}

