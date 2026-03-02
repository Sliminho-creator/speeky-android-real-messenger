package com.speeky.app.core

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class AppUser(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val isSystem: Boolean = false
)

@Serializable
enum class MessageKind {
    TEXT,
    VOICE
}

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val peerId: String,
    val fromMe: Boolean,
    val kind: MessageKind = MessageKind.TEXT,
    val text: String = "",
    val audioPath: String? = null,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class ThemePreset {
    DARK,
    VIOLET,
    OCEAN,
    SAKURA
}

@Serializable
data class AppSnapshot(
    val users: List<AppUser>,
    val currentUserId: String? = null,
    val selectedTheme: ThemePreset = ThemePreset.VIOLET,
    val messages: List<ChatMessage> = emptyList()
) {
    companion object {
        fun seed(): AppSnapshot {
            val me = AppUser(
                id = "me",
                name = "Sliminho",
                username = "sliminho",
                email = "hello@speeky.app",
                password = "password123"
            )
            val forever = AppUser(
                id = "forever",
                name = "forever",
                username = "forever",
                email = "forever@speeky.app",
                password = "forever123"
            )
            val saved = AppUser(
                id = "saved",
                name = "Избранное",
                username = "saved",
                email = "",
                password = "",
                isSystem = true
            )
            return AppSnapshot(
                users = listOf(me, forever, saved),
                currentUserId = null,
                selectedTheme = ThemePreset.VIOLET,
                messages = listOf(
                    ChatMessage(peerId = "forever", fromMe = false, text = "маму ем", timestamp = 1710000000000),
                    ChatMessage(peerId = "forever", fromMe = false, text = "все норм", timestamp = 1710000300000),
                    ChatMessage(peerId = "forever", fromMe = false, text = "завтра узнаем", timestamp = 1710000600000),
                    ChatMessage(peerId = "forever", fromMe = true, text = "как узнал ?", timestamp = 1710000900000),
                    ChatMessage(peerId = "forever", fromMe = true, text = "время ж поздно уже за комп та не пустят", timestamp = 1710001200000),
                    ChatMessage(peerId = "saved", fromMe = false, text = "Тут будут сохранённые сообщения", timestamp = 1710000000000)
                )
            )
        }
    }
}
