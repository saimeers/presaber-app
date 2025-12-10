package com.example.presaber.data.local

import android.content.Context
import org.json.JSONObject

object SessionLocalCache {
    private const val PREF_NAME = "session_cache"
    private const val KEY_ANSWERS_PREFIX = "session_answers_"
    private const val KEY_INDEX_PREFIX = "session_index_"

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
            .apply()
    }
}

