package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EasyBookApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EasyBookViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EasyBookViewModel = viewModel()
            val isDark by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDark) {
                EasyBookApp(viewModel)
            }
        }
    }
}
