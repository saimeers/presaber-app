package com.example.presaber.utils

import android.content.Context
import androidx.core.content.edit

object SimulacroSessionManager {
    private const val PREFS_NAME = "simulacro_student_prefs"
    private const val KEY_ACTIVE_SIMULACRO_ID = "active_simulacro_id"
    private const val KEY_CURRENT_QUESTION_INDEX = "current_question_index"

    fun saveActiveSession(context: Context, idSimulacro: Int) {
        // 1. Obtenemos la referencia a las preferencias
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 2. Usamos 'prefs' para leer y editar
        prefs.edit {
            putInt(KEY_ACTIVE_SIMULACRO_ID, idSimulacro)

            // CORRECCIÓN: Usamos el objeto 'prefs' para verificar si existe
            if (!prefs.contains(KEY_CURRENT_QUESTION_INDEX)) {
                putInt(KEY_CURRENT_QUESTION_INDEX, 0)
            }
        }
    }

    fun getActiveSimulacroId(context: Context): Int? {
        val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_ACTIVE_SIMULACRO_ID, -1)
        return if (id != -1) id else null
    }

    fun saveCurrentProgress(context: Context, index: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putInt(KEY_CURRENT_QUESTION_INDEX, index)
        }
    }

    fun getCurrentIndex(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CURRENT_QUESTION_INDEX, 0)
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            remove(KEY_ACTIVE_SIMULACRO_ID)
            remove(KEY_CURRENT_QUESTION_INDEX)
        }
    }
}