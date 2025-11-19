package de.doetchen.project.manager

import de.doetchen.project.Needed
import de.doetchen.project.extensions.CommandBuilder
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.command.brigadier.Commands

class CommandRegistry(private val plugin: Needed) {

    private val commands = mutableListOf<CommandRegistration>()

    fun command(
        module: String = "",
        builder: (Needed) -> CommandBuilder
    ) {
        commands.add(CommandRegistration(module, builder))
    }

    fun registerAll(event: ReloadableRegistrarEvent<Commands>) {
        val registrar = event.registrar()

        commands.forEach { registration ->
            if (registration.module.isNotEmpty() &&
                !plugin.moduleManager.isModuleEnabled(registration.module)
            ) {
                return@forEach
            }

            try {
                val command = registration.builder(plugin)
                registrar.register(command.register(), command.description, command.aliases)
                plugin.logger.info("✓ Registered command from ${command::class.simpleName}")
            } catch (e: Exception) {
                plugin.logger.severe("✗ Failed to register command: ${e.message}")
            }
        }
    }

    private data class CommandRegistration(
        val module: String,
        val builder: (Needed) -> CommandBuilder
    )
}