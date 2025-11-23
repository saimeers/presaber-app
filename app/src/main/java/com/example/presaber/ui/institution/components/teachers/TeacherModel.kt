package com.example.presaber.ui.institution.components.teachers

data class Teacher(
    val name: String,
    val imageRes: Int,
    val photoUrl: String? = null
)


val teacherList = listOf(
    Teacher("Henry Alexander", com.example.presaber.R.drawable.icon_user),
    Teacher("Vargas Lleras", com.example.presaber.R.drawable.icon_user),
    Teacher("Saimer", com.example.presaber.R.drawable.icon_user),
    Teacher("Omar Calculo", com.example.presaber.R.drawable.icon_user),
    Teacher("Sebastian Pabon", com.example.presaber.R.drawable.icon_user),
    Teacher("Lopez", com.example.presaber.R.drawable.icon_user),
    Teacher("Michelle Rojas", com.example.presaber.R.drawable.icon_user)
)
