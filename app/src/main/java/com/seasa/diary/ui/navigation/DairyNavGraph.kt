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

package com.seasa.diary.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.home.HomeDestination
import com.seasa.diary.ui.home.HomeScreen
import com.seasa.diary.ui.note.NoteDetailsDestination
import com.seasa.diary.ui.note.NoteDetailsScreen
import com.seasa.diary.ui.note.NoteEditDestination
import com.seasa.diary.ui.note.NoteEditScreen
import com.seasa.diary.ui.note.NoteEntryDestination
import com.seasa.diary.ui.note.NoteEntryScreen
import com.seasa.diary.ui.setting.BackupDestination
import com.seasa.diary.ui.setting.BackupScreen
import com.seasa.diary.ui.setting.BackupViewModel
import com.seasa.diary.ui.setting.FontDestination
import com.seasa.diary.ui.setting.FontScreen
import com.seasa.diary.ui.setting.FontViewModel
import com.seasa.diary.ui.setting.SettingDestination
import com.seasa.diary.ui.setting.SettingScreen

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DiaryNavHost(
    navController: NavHostController,
    fontViewModel: FontViewModel,
    modifier: Modifier = Modifier,
) {
    val backupViewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory)

    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToItemEntry = { navController.navigate(NoteEntryDestination.route) },
                navigateToItemUpdate = {
                    navController.navigate("${NoteDetailsDestination.route}/${it}")
                }
            )
        }
        composable(route = NoteEntryDestination.route) {
            NoteEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = NoteDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(NoteDetailsDestination.noteIdArg) {
                type = NavType.IntType
            })
        ) {
            NoteDetailsScreen(
                navigateToEditNote = { navController.navigate("${NoteEditDestination.route}/$it") },
                navigateBack = { navController.navigateUp() }
            )
        }
        composable(
            route = NoteEditDestination.routeWithArgs,
            arguments = listOf(navArgument(NoteEditDestination.noteIdArg) {
                type = NavType.IntType
            })
        ) {
            NoteEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = SettingDestination.route,
        ) {
            SettingScreen(
                navigateToSignIn = { navController.navigate(BackupDestination.route) },
                navigateToFont = { navController.navigate(FontDestination.route) },
            )
        }
        composable(
            route = BackupDestination.route,
        ) {
            BackupScreen(
                backupViewModel=backupViewModel,
                onSignInClick = {
                    backupViewModel.handleGoogleSignIn(navController.context)
                    Log.d("BackupScreen", "onSignInClick triggered")
                }
            )
        }
        composable(
            route = FontDestination.route,
        ) {
            FontScreen(
                fontViewModel=fontViewModel,
            )
        }
    }
}
