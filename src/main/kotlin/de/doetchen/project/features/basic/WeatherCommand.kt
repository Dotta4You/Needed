package de.doetchen.project.features.basic

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Sound
import org.bukkit.entity.Player

class WeatherCommand(private val plugin: Needed) : CommandBuilder {

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("weather")
            .requires { it.sender is Player && it.sender.hasPermission("needed.weather") }
            .then(
                Commands.argument("type", StringArgumentType.word())
                    .suggests { _, builder ->
                        listOf("clear", "rainy", "thunder").forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val player = context.source.sender as Player
                        val world = player.world
                        val type = StringArgumentType.getString(context, "type")

                        when (type.lowercase()) {
                            "clear" -> {
                                world.setStorm(false)
                                world.isThundering = false
                                world.clearWeatherDuration = 999999

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("weather.clear"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("☀ ", NamedTextColor.YELLOW))
                                    .append(Component.text("Clear Sky", NamedTextColor.AQUA, TextDecoration.BOLD))
                                    .append(Component.text(" ☀", NamedTextColor.YELLOW))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            "rainy" -> {
                                world.setStorm(true)
                                world.isThundering = false
                                world.weatherDuration = 999999

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.WEATHER_RAIN, 0.5f, 1.0f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("weather.rainy"))
                                player.sendMessage(message)

                                val actionBar = Component.text()
                                    .append(Component.text("☔ ", NamedTextColor.BLUE))
                                    .append(Component.text("Rainy", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                                    .append(Component.text(" ☔", NamedTextColor.BLUE))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            "thunder" -> {
                                world.setStorm(true)
                                world.isThundering = true
                                world.thunderDuration = 999999

                                if (plugin.config.getBoolean("settings.sounds.enabled", true)) {
                                    player.playSound(player.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.0f)
                                }

                                val message = plugin.languageManager.getPrefix()
                                    .append(plugin.languageManager.getMessage("weather.thunder"))
                                player.sendMessage(message)

                                val actionBarText = plugin.languageManager.getMessage("weather.actionbar.thunder")
                                val actionBar = Component.text()
                                    .append(Component.text("⚡ ", NamedTextColor.WHITE))
                                    .append(actionBarText.color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD))
                                    .append(Component.text(" ⚡", NamedTextColor.WHITE))
                                    .build()
                                player.sendActionBar(actionBar)
                            }
                            else -> return@executes 0
                        }

                        1
                    }
            )
            .build()
    }

    override val aliases = listOf("w")
    override val description = "Change weather"
}

