package com.example.presaber

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.presaber.navigation.MainNavigation
import com.example.presaber.ui.theme.PresaberTheme
import com.example.presaber.ui.institution.InstitutionNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PresaberTheme {
                //MainNavigation()
                InstitutionNavHost(idInstitucion=1)
            }
        }
    }

}
