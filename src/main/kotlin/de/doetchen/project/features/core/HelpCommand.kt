package de.doetchen.project.features.core

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import de.doetchen.project.Needed
import de.doetchen.project.core.extensions.CommandBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender

class HelpCommand(private val plugin: Needed) : CommandBuilder {

    private data class CommandInfo(
        val command: String,
        val aliases: List<String>,
        val description: String,
        val permission: String,
        val category: String
    )

    private val commands = listOf(
        CommandInfo("/gm <mode> [player]", listOf("/gamemode"), "Spielmodus ändern", "needed.gamemode", "Basic"),
        CommandInfo("/time <period>", listOf(), "Tageszeit ändern", "needed.time", "Basic"),
        CommandInfo("/weather <type>", listOf("/w"), "Wetter ändern", "needed.weather", "Basic"),

        CommandInfo("/message <player> <msg>", listOf("/msg", "/tell", "/whisper", "/w", "/pm"), "Private Nachricht senden", "needed.message", "Messaging"),
        CommandInfo("/reply <message>", listOf("/r"), "Auf letzte Nachricht antworten", "needed.reply", "Messaging"),

        CommandInfo("/vanish [player]", listOf("/v"), "Unsichtbar werden", "needed.vanish", "Moderation"),
        CommandInfo("/freeze <player>", listOf("/ss", "/screenshare"), "Spieler einfrieren", "needed.freeze", "Moderation"),
        CommandInfo("/invsee <player>", listOf("/openinv", "/inv"), "Inventar ansehen", "needed.invsee", "Moderation"),
        CommandInfo("/clear [player]", listOf("/ci", "/clearinv"), "Inventar leeren", "needed.clear", "Moderation"),
        CommandInfo("/fly [player]", listOf(), "Flugmodus umschalten", "needed.fly", "Moderation"),
        CommandInfo("/god [player]", listOf("/invulnerable"), "Gottmodus umschalten", "needed.god", "Moderation"),
        CommandInfo("/speed <amount> [player]", listOf(), "Geschwindigkeit ändern", "needed.speed", "Moderation"),
        CommandInfo("/heal [player]", listOf(), "Spieler heilen", "needed.heal", "Moderation"),
        CommandInfo("/feed [player]", listOf("/eat"), "Spieler sättigen", "needed.feed", "Moderation"),
        CommandInfo("/tp <player> [destination]", listOf("/teleport"), "Zu Spieler teleportieren", "needed.tp", "Moderation"),
        CommandInfo("/tphere <player>", listOf("/s", "/summon"), "Spieler zu sich teleportieren", "needed.tphere", "Moderation"),
        CommandInfo("/kick <player> [reason]", listOf(), "Spieler kicken", "needed.kick", "Moderation"),
        CommandInfo("/broadcast <message>", listOf("/bc", "/announce"), "Server-Broadcast", "needed.broadcast", "Moderation")
    )

    private val commandsPerPage = 7

    override fun register(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("needed")
            .then(
                Commands.literal("help")
                    .executes { context ->
                        sendHelpPage(context.source.sender, 1)
                        1
                    }
                    .then(
                        Commands.argument("page", IntegerArgumentType.integer(1))
                            .executes { context ->
                                val page = IntegerArgumentType.getInteger(context, "page")
                                sendHelpPage(context.source.sender, page)
                                1
                            }
                    )
            )
            .build()
    }

    private fun sendHelpPage(sender: CommandSender, page: Int) {
        val totalPages = (commands.size + commandsPerPage - 1) / commandsPerPage
        val validPage = page.coerceIn(1, totalPages)

        val startIndex = (validPage - 1) * commandsPerPage
        val endIndex = (startIndex + commandsPerPage).coerceAtMost(commands.size)
        val pageCommands = commands.subList(startIndex, endIndex)

        sender.sendMessage(Component.empty())
        sender.sendMessage(buildHeader(validPage, totalPages))
        sender.sendMessage(Component.empty())

        var lastCategory = ""
        pageCommands.forEach { cmd ->
            if (cmd.category != lastCategory) {
                sender.sendMessage(buildCategoryHeader(cmd.category))
                lastCategory = cmd.category
            }
            sender.sendMessage(buildCommandLine(cmd))
        }

        sender.sendMessage(Component.empty())
        sender.sendMessage(buildFooter(validPage, totalPages))
        sender.sendMessage(Component.empty())
    }

    private fun buildHeader(page: Int, totalPages: Int): Component {
        return Component.text()
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .appendNewline()
            .append(Component.text("           ", NamedTextColor.GRAY))
            .append(Component.text("⚡ ", NamedTextColor.YELLOW))
            .append(Component.text("Needed", NamedTextColor.GREEN, TextDecoration.BOLD))
            .append(Component.text(" Help", NamedTextColor.GRAY))
            .append(Component.text(" ⚡", NamedTextColor.YELLOW))
            .appendNewline()
            .append(Component.text("                    ", NamedTextColor.GRAY))
            .append(Component.text("Seite ", NamedTextColor.GRAY))
            .append(Component.text("$page", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text(" / ", NamedTextColor.DARK_GRAY))
            .append(Component.text("$totalPages", NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .build()
    }

    private fun buildCategoryHeader(category: String): Component {
        val emoji = when (category) {
            "Basic" -> "🎮"
            "Messaging" -> "💬"
            "Moderation" -> "🛡️"
            else -> "📦"
        }

        return Component.text()
            .append(Component.text(" $emoji ", NamedTextColor.YELLOW))
            .append(Component.text(category, NamedTextColor.AQUA, TextDecoration.BOLD))
            .build()
    }

    private fun buildCommandLine(cmd: CommandInfo): Component {
        val commandText = Component.text()
            .append(Component.text("  • ", NamedTextColor.DARK_GRAY))
            .append(Component.text(cmd.command, NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(cmd.command.split(" ")[0]))
                .hoverEvent(HoverEvent.showText(
                    Component.text()
                        .append(Component.text("Click to insert command", NamedTextColor.YELLOW))
                        .appendNewline()
                        .append(Component.text("Permission: ", NamedTextColor.GRAY))
                        .append(Component.text(cmd.permission, NamedTextColor.GOLD))
                        .build()
                ))
            )
            .build()

        val descriptionText = Component.text()
            .append(Component.text("    ", NamedTextColor.GRAY))
            .append(Component.text("└ ", NamedTextColor.DARK_GRAY))
            .append(Component.text(cmd.description, NamedTextColor.GRAY))
            .build()

        val result = Component.text()
            .append(commandText)
            .appendNewline()
            .append(descriptionText)

        if (cmd.aliases.isNotEmpty()) {
            val aliasesText = Component.text()
                .append(Component.text("      ", NamedTextColor.GRAY))
                .append(Component.text("Aliases: ", NamedTextColor.DARK_GRAY))
                .append(Component.text(cmd.aliases.joinToString(", "), NamedTextColor.DARK_AQUA))
                .build()
            result.appendNewline().append(aliasesText)
        }

        return result.build()
    }

    private fun buildFooter(page: Int, totalPages: Int): Component {
        val footer = Component.text()
            .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
            .appendNewline()
            .append(Component.text("  ", NamedTextColor.GRAY))

        if (page > 1) {
            footer.append(
                Component.text("« Zurück", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/needed help ${page - 1}"))
                    .hoverEvent(HoverEvent.showText(Component.text("Zur vorherigen Seite", NamedTextColor.GOLD)))
            )
        } else {
            footer.append(Component.text("« Zurück", NamedTextColor.DARK_GRAY))
        }

        footer.append(Component.text("        ", NamedTextColor.GRAY))
        footer.append(Component.text("⚡", NamedTextColor.GOLD))
        footer.append(Component.text("        ", NamedTextColor.GRAY))

        if (page < totalPages) {
            footer.append(
                Component.text("Weiter »", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/needed help ${page + 1}"))
                    .hoverEvent(HoverEvent.showText(Component.text("Zur nächsten Seite", NamedTextColor.GOLD)))
            )
        } else {
            footer.append(Component.text("Weiter »", NamedTextColor.DARK_GRAY))
        }

        footer.appendNewline()
        footer.append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))

        return footer.build()
    }

    override val aliases = listOf("?", "hilfe")
    override val description = "Show help menu"
}

