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

package com.seasa.diary.ui.note

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seasa.diary.LoadingScreen
import com.seasa.diary.R
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.navigation.NavigationDestination
import com.seasa.diary.ui.theme.DiaryTheme

object NoteEditDestination : NavigationDestination {
    override val route = "note_edit"
    override val titleRes = R.string.edit_note_title
    const val noteIdArg = "noteId"
    val routeWithArgs = "$route/{$noteIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: NoteEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    if(viewModel.noteUiState.isLoading){
        LoadingScreen()
    } else {
        NoteEntryScreen(
            navigateBack=navigateBack,
            onNavigateUp=onNavigateUp,
            canNavigateBack=true,
            dateSelectEnabled=false,
            viewModel=viewModel )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditScreenPreview() {
    DiaryTheme {
        NoteEditScreen(navigateBack = { /*Do nothing*/ }, onNavigateUp = { /*Do nothing*/ })
    }
}
