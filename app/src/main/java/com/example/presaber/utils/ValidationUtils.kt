package com.example.presaber.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    fun validatePassword(password: String): PasswordValidation {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> PasswordStrength.STRONG
            hasMinLength && (hasUpperCase || hasLowerCase) && hasDigit -> PasswordStrength.MEDIUM
            hasMinLength -> PasswordStrength.WEAK
            else -> PasswordStrength.INVALID
        }

        return PasswordValidation(
            isValid = hasMinLength && hasUpperCase && hasLowerCase && hasDigit,
            strength = strength,
            message = when (strength) {
                PasswordStrength.INVALID -> "Mínimo 8 caracteres"
                PasswordStrength.WEAK -> "Contraseña débil"
                PasswordStrength.MEDIUM -> "Contraseña media"
                PasswordStrength.STRONG -> "Contraseña segura"
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertMillisToDate(millis: Long): String {
        val localDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return localDate.format(formatter)
    }
}

data class PasswordValidation(
    val isValid: Boolean,
    val strength: PasswordStrength,
    val message: String
)

enum class PasswordStrength {
    INVALID, WEAK, MEDIUM, STRONG
}