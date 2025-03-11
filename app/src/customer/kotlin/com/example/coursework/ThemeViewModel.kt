package com.example.coursework

import androidx.lifecycle.ViewModel
import com.example.coursework.ui.theme.AppThemeType
import com.example.coursework.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeManager: ThemeManager
) : ViewModel() {
    val currentTheme: StateFlow<AppThemeType> = themeManager.currentTheme

    fun toggleTheme() {
        themeManager.toggleTheme()
    }
}