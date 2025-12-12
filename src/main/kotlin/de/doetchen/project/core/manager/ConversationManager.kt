package de.doetchen.project.core.manager

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ConversationManager {

    private val conversations = ConcurrentHashMap<UUID, UUID>()

    fun setLastConversation(sender: UUID, receiver: UUID) {
        conversations[sender] = receiver
    }

    fun getLastConversation(sender: UUID): UUID? {
        return conversations[sender]
    }

    fun hasConversation(sender: UUID): Boolean {
        return conversations.containsKey(sender)
    }

    fun clearConversation(sender: UUID) {
        conversations.remove(sender)
    }

    fun clear() {
        conversations.clear()
    }
}

