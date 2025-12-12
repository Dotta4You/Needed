package de.doetchen.project.core.manager

import de.doetchen.project.Needed
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ModuleManager(private val plugin: Needed) {

    private val modulesFolder: File = File(plugin.dataFolder, "modules")
    private val moduleConfigs = mutableMapOf<String, YamlConfiguration>()

    private val availableModules = listOf(
        "gamemode", "weather", "time",
        "messaging",
        "vanish", "freeze", "fly", "god", "speed", "heal", "feed",
        "invsee", "clear", "teleport", "teleporthere", "tpall", "kick", "broadcast"
    )

    init {
        if (!modulesFolder.exists()) {
            modulesFolder.mkdirs()
        }
        loadAllModules()
    }

    private fun loadAllModules() {
        availableModules.forEach { moduleName ->
            val moduleFile = File(modulesFolder, "$moduleName.yml")

            if (!moduleFile.exists()) {
                try {
                    plugin.saveResource("modules/$moduleName.yml", false)
                } catch (_: Exception) {
                    createDefaultModuleConfig(moduleFile, moduleName)
                }
            }

            moduleConfigs[moduleName] = YamlConfiguration.loadConfiguration(moduleFile)
        }
    }

    private fun createDefaultModuleConfig(file: File, moduleName: String) {
        val config = YamlConfiguration()
        config.set("enabled", true)
        config.set("description", "Module: $moduleName")
        config.save(file)
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        val cleanName = moduleName.substringAfterLast(".")
        return moduleConfigs[cleanName]?.getBoolean("enabled", true) ?: true
    }

    fun getModuleConfig(moduleName: String): YamlConfiguration? {
        val cleanName = moduleName.substringAfterLast(".")
        return moduleConfigs[cleanName]
    }

    fun getAllModules(): List<ModuleInfo> {
        return availableModules.map { moduleName ->
            val config = moduleConfigs[moduleName]
            ModuleInfo(
                name = moduleName,
                category = getCategoryForModule(moduleName),
                enabled = config?.getBoolean("enabled", true) ?: true
            )
        }
    }

    private fun getCategoryForModule(moduleName: String): String {
        return when (moduleName) {
            "gamemode", "weather", "time" -> "Basic"
            "messaging" -> "Messaging"
            "vanish", "freeze", "fly", "god", "speed", "heal", "feed",
            "invsee", "clear", "teleport", "teleporthere", "tpall", "kick", "broadcast" -> "Moderation"
            else -> "Other"
        }
    }

    fun reload() {
        moduleConfigs.clear()
        loadAllModules()
    }

    fun getEnabledModulesCount(): Int {
        return moduleConfigs.values.count { it.getBoolean("enabled", true) }
    }

    fun getModuleUsageMap(): Map<String, Int> {
        val usageMap = mutableMapOf<String, Int>()

        availableModules.forEach { moduleName ->
            val config = moduleConfigs[moduleName]
            if (config?.getBoolean("enabled", true) == true) {
                val category = getCategoryForModule(moduleName)
                usageMap[category] = usageMap.getOrDefault(category, 0) + 1
            }
        }

        return usageMap
    }

    data class ModuleInfo(
        val name: String,
        val category: String,
        val enabled: Boolean = true
    )
}

