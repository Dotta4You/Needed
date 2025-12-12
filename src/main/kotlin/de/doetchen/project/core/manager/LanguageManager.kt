package de.doetchen.project.core.manager

import de.doetchen.project.Needed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class LanguageManager(private val plugin: Needed) {

    private val miniMessage = MiniMessage.miniMessage()
    private val messagesFolder = File(plugin.dataFolder, "messages")
    private lateinit var messages: YamlConfiguration
    private lateinit var language: String

    init {
        initializeLanguageFiles()
        loadLanguage()
    }

    private fun initializeLanguageFiles() {
        if (!messagesFolder.exists()) {
            messagesFolder.mkdirs()
        }

        val availableLanguages = listOf("en", "de")

        for (lang in availableLanguages) {
            val langFile = File(messagesFolder, "$lang.yml")
            if (!langFile.exists()) {
                try {
                    plugin.saveResource("messages/$lang.yml", false)
                    plugin.logger.info("Created language file: $lang.yml")
                } catch (_: Exception) {
                    plugin.logger.warning("Could not create language file: $lang.yml")
                }
            }
        }
    }

    fun loadLanguage() {
        language = plugin.config.getString("settings.language", "en") ?: "en"

        plugin.logger.info("Loading language: $language")

        if (!messagesFolder.exists()) {
            messagesFolder.mkdirs()
        }

        val languageFile = File(messagesFolder, "$language.yml")
        if (!languageFile.exists()) {
            try {
                plugin.saveResource("messages/$language.yml", false)
                plugin.logger.info("Created language file: $language.yml")
            } catch (_: Exception) {
                plugin.logger.warning("Could not find language file for '$language', falling back to 'en'")
                language = "en"
                val fallbackFile = File(messagesFolder, "en.yml")
                if (!fallbackFile.exists()) {
                    plugin.saveResource("messages/en.yml", false)
                }
                messages = YamlConfiguration.loadConfiguration(fallbackFile)
                return
            }
        }

        messages = YamlConfiguration.loadConfiguration(languageFile)
        plugin.logger.info("Language file loaded successfully: $language.yml")
    }

    fun getMessage(key: String, vararg replacements: Pair<String, String>): Component {
        var message = messages.getString(key) ?: key

        replacements.forEach { (placeholder, value) ->
            message = message.replace("{$placeholder}", value)
        }

        return miniMessage.deserialize(message)
    }

    fun getPrefix(): Component {
        val prefix = plugin.config.getString("settings.prefix", "<gray>[<gradient:#00ff87:#60efff>Needed</gradient>]</gray> ")
            ?: "<gray>[<gradient:#00ff87:#60efff>Needed</gradient>]</gray> "
        return miniMessage.deserialize(prefix)
    }

    fun reload() {
        loadLanguage()
    }
}

