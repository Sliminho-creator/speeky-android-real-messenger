package com.speeky.app.core

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(val id: String, val email: String, val username: String, val displayName: String, val avatarColor: String? = null)

@Serializable
data class AuthResponse(val token: String, val user: AuthUser)

@Serializable
data class SearchUser(val id: String, val username: String, val displayName: String, val avatarColor: String? = null)

@Serializable
data class DirectMessage(val id: String, val chatId: String, val senderId: String, val text: String, val type: String, val fileUrl: String? = null, val fileName: String? = null, val durationMs: Int? = null, val createdAt: String)

@Serializable
data class ChatPeer(val id: String, val username: String, val displayName: String, val avatarColor: String? = null, val lastSeen: String)

@Serializable
data class ChatItem(val id: String, val peer: ChatPeer, val lastMessage: String, val updatedAt: String, val messages: List<DirectMessage> = emptyList())
