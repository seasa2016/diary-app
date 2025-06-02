/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seasa.diary.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.seasa.diary.DiaryTopAppBar
import com.seasa.diary.R
import com.seasa.diary.ui.navigation.NavigationDestination

object SettingDestination : NavigationDestination {
    override val route = "setting"
    override val titleRes = R.string.setting_title
}

/**
 * Entry route for Setting screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navigateToSignIn: () -> Unit,
    navigateToFont: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiaryTopAppBar(
                title = stringResource(SettingDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        SettingBody(
            navigateToSignIn = navigateToSignIn,
            navigateToFont = navigateToFont,
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding,
        )
    }
}

@Composable
private fun SettingBody(
    navigateToSignIn: () -> Unit,
    navigateToFont: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.padding(contentPadding))
        SettingsListItem(
            icon = ImageVector.vectorResource(id= R.drawable.database_svgrepo_com),
            iconTint = Color(0x00FF9800),
            text = "Backup",
            onClick = navigateToSignIn,
            modifier = Modifier.fillMaxWidth()
        )
        SettingsListItem(
            icon = ImageVector.vectorResource(id= R.drawable.font),
            iconTint = Color(0x00FF9800),
            text = "Font",
            onClick = navigateToFont,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
