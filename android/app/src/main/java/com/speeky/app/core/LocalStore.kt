package com.speeky.app.core

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object LocalStore {
    private const val PREFS_NAME = "speeky_local_store"
    private const val SNAPSHOT_KEY = "snapshot_json"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(context: Context): AppSnapshot {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(SNAPSHOT_KEY, null) ?: return AppSnapshot.seed()
        return runCatching { json.decodeFromString<AppSnapshot>(raw) }
            .getOrElse { AppSnapshot.seed() }
    }

    fun save(context: Context, snapshot: AppSnapshot) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SNAPSHOT_KEY, json.encodeToString(snapshot)).apply()
    }

    fun reset(context: Context) {
        save(context, AppSnapshot.seed())
    }
}
