package de.doetchen.project.manager

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
        loadLanguage()
    }

    fun loadLanguage() {
        language = plugin.config.getString("settings.language", "en") ?: "en"

        if (!messagesFolder.exists()) {
            messagesFolder.mkdirs()
        }

        val languageFile = File(messagesFolder, "$language.yml")
        if (!languageFile.exists()) {
            plugin.saveResource("messages/$language.yml", false)
        }

        messages = YamlConfiguration.loadConfiguration(languageFile)
    }

    fun getMessage(key: String, vararg replacements: Pair<String, String>): Component {
        var message = messages.getString(key) ?: key

        replacements.forEach { (placeholder, value) ->
            message = message.replace("{$placeholder}", value)
        }

        return miniMessage.deserialize(message)
    }

    fun getPrefix(): Component {
        val prefix = plugin.config.getString("settings.prefix", "<gray>[<green>Needed</green>]</gray>") ?: "<gray>[<green>Needed</green>]</gray>"
        return miniMessage.deserialize(prefix)
    }

    fun reload() {
        loadLanguage()
    }
}
