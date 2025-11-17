package de.doetchen.project.manager

import de.doetchen.project.Needed
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ModuleManager(private val plugin: Needed) {

    private val modulesFile: File = File(plugin.dataFolder, "modules.yml")
    private val modulesConfig: YamlConfiguration

    init {
        if (!modulesFile.exists()) {
            plugin.saveResource("modules.yml", false)
        }
        modulesConfig = YamlConfiguration.loadConfiguration(modulesFile)
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        return modulesConfig.getBoolean("modules.$moduleName", true)
    }

    fun reload() {
        modulesConfig.load(modulesFile)
    }
}
