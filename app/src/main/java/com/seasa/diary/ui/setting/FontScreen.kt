package com.seasa.diary.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.seasa.diary.DiaryTopAppBar
import com.seasa.diary.R
import com.seasa.diary.ui.navigation.NavigationDestination

object FontDestination : NavigationDestination {
    override val route = "font"
    override val titleRes = R.string.font
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontScreen(
    fontViewModel: FontViewModel,
) {
    val fontFamily by fontViewModel.fontFamily.collectAsState()
    val fontSize by fontViewModel.fontSize.collectAsState()

    val availableFonts = listOf("Default", "Serif", "Monospace")

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiaryTopAppBar(
                title = stringResource(FontDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("Font Family")
            availableFonts.forEach { font ->
                Row {
                    RadioButton(
                        selected = fontFamily == font,
                        onClick = { fontViewModel.updateFontSettings(font, fontSize) }
                    )
                    Text(font)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Font Size")
            Slider(
                value = fontSize,
                onValueChange = { fontViewModel.updateFontSettings(fontFamily, it) },
                valueRange = 12f..24f
            )
        }
    }
}
