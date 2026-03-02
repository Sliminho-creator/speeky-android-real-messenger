package com.speeky.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.speeky.app.ui.SpeekyApp
import com.speeky.app.ui.theme.SpeekyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SpeekyTheme { SpeekyApp() } }
    }
}
