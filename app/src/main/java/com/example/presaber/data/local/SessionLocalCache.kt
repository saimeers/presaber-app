package com.example.presaber.data.local

import android.content.Context
import org.json.JSONObject

object SessionLocalCache {
    private const val PREF_NAME = "session_cache"
    private const val KEY_ANSWERS_PREFIX = "session_answers_"
    private const val KEY_INDEX_PREFIX = "session_index_"
    private const val KEY_TIMER_PREFIX = "session_timer_"
    private const val KEY_TIMER_SAVED_AT_PREFIX = "session_timer_saved_at_"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveAnswer(context: Context, sessionId: Int, questionId: Int, optionId: Int) {
        val pref = prefs(context)
        val key = "$KEY_ANSWERS_PREFIX$sessionId"
        val current = pref.getString(key, "{}") ?: "{}"
        val json = JSONObject(current)
        json.put(questionId.toString(), optionId)
        pref.edit().putString(key, json.toString()).apply()
    }

    fun loadAnswers(context: Context, sessionId: Int): Map<Int, Int> {
        val pref = prefs(context)
        val key = "$KEY_ANSWERS_PREFIX$sessionId"
        val current = pref.getString(key, "{}") ?: "{}"
        val json = JSONObject(current)
        val result = mutableMapOf<Int, Int>()
        json.keys().forEach { k ->
            result[k.toInt()] = json.optInt(k)
        }
        return result
    }

    fun saveIndex(context: Context, sessionId: Int, index: Int) {
        prefs(context).edit().putInt("$KEY_INDEX_PREFIX$sessionId", index).apply()
    }

    fun loadIndex(context: Context, sessionId: Int): Int? {
        val pref = prefs(context)
        val key = "$KEY_INDEX_PREFIX$sessionId"
        return if (pref.contains(key)) pref.getInt(key, 0) else null
    }

    fun clearSession(context: Context, sessionId: Int) {
        prefs(context).edit()
            .remove("$KEY_ANSWERS_PREFIX$sessionId")
            .remove("$KEY_INDEX_PREFIX$sessionId")
            .remove("$KEY_TIMER_PREFIX$sessionId")
            .remove("$KEY_TIMER_SAVED_AT_PREFIX$sessionId")
            .apply()
    }

    fun saveTimer(context: Context, sessionId: Int, remainingSeconds: Int) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putInt("$KEY_TIMER_PREFIX$sessionId", remainingSeconds)
            .putLong("$KEY_TIMER_SAVED_AT_PREFIX$sessionId", now)
            .apply()
    }

    fun loadTimer(context: Context, sessionId: Int): Int? {
        val pref = prefs(context)
        val remaining = pref.getInt("$KEY_TIMER_PREFIX$sessionId", -1)
        if (remaining < 0) return null
        val savedAt = pref.getLong("$KEY_TIMER_SAVED_AT_PREFIX$sessionId", 0L)
        if (savedAt == 0L) return null
        val elapsedSeconds = ((System.currentTimeMillis() - savedAt) / 1000).toInt()
        val adjusted = remaining - elapsedSeconds
        return if (adjusted > 0) adjusted else 0
    }

    fun clearTimer(context: Context, sessionId: Int) {
        prefs(context).edit()
            .remove("$KEY_TIMER_PREFIX$sessionId")
            .remove("$KEY_TIMER_SAVED_AT_PREFIX$sessionId")
            .apply()
    }
}

