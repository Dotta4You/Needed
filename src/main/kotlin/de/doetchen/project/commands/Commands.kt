package de.doetchen.project.commands

import de.doetchen.project.manager.CommandRegistry

fun CommandRegistry.registerCommands() {
    command(module = "gamemode") { GameModeCommand(it) }
    command(module = "weather") { WeatherCommand(it) }
    command(module = "time") { TimeCommand(it) }
}

