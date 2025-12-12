package de.doetchen.project.metrics

import de.doetchen.project.Needed
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bstats.charts.SingleLineChart
import org.bstats.charts.AdvancedPie

class MetricsHandler(private val plugin: Needed) {

    private var metrics: Metrics? = null
    private val serviceId = 28174

    fun initialize() {
        if (metrics != null) {
            plugin.logger.warning("Metrics already initialized!")
            return
        }

        try {
            metrics = Metrics(plugin, serviceId)

            metrics?.addCustomChart(SimplePie("language") {
                plugin.config.getString("settings.language", "en") ?: "en"
            })

            metrics?.addCustomChart(SingleLineChart("enabled_modules") {
                plugin.moduleManager.getEnabledModulesCount()
            })

            metrics?.addCustomChart(AdvancedPie("module_usage") {
                plugin.moduleManager.getModuleUsageMap()
            })

            metrics?.addCustomChart(SimplePie("server_software") {
                plugin.server.name
            })

            metrics?.addCustomChart(SimplePie("sounds_enabled") {
                if (plugin.config.getBoolean("settings.sounds.enabled", true)) "Enabled" else "Disabled"
            })

            plugin.logger.info("bStats Metrics initialized successfully!")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize bStats Metrics: ${e.message}")
            e.printStackTrace()
        }
    }

    fun shutdown() {
        metrics?.shutdown()
        metrics = null
        plugin.logger.info("bStats Metrics shutdown.")
    }

    fun isEnabled(): Boolean = metrics != null
}

