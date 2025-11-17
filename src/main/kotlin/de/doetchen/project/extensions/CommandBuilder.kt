package de.doetchen.project.extensions

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

interface CommandBuilder {

    fun register(): LiteralCommandNode<CommandSourceStack>

    val children: List<CommandBuilder>
        get() = listOf()
    
    val aliases: List<String>
        get() = listOf()

    val description: String
        get() = ""
}