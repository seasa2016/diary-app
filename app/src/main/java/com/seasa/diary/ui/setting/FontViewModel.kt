package com.seasa.diary.ui.setting

import androidx.lifecycle.ViewModel
import com.seasa.diary.data.FontRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FontViewModel(
    private val fontRepository: FontRepository,
) : ViewModel() {

    private val _fontFamily = MutableStateFlow("Default")
    private val _fontSize = MutableStateFlow(16f)

    val fontFamily: StateFlow<String> = _fontFamily
    val fontSize: StateFlow<Float> = _fontSize

    init {
        viewModelScope.launch {
            _fontFamily.value = fontRepository.fontFamily.firstOrNull()?:"default"
            _fontSize.value = fontRepository.fontSize.firstOrNull()?:16f
        }
    }

    fun updateFontSettings(family: String, size: Float) {
        _fontFamily.value = family
        _fontSize.value = size
        viewModelScope.launch {
            fontRepository.saveFontState(family, size)
        }
    }
}
