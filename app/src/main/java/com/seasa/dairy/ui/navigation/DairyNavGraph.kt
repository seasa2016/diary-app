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

package com.seasa.dairy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.seasa.dairy.ui.home.HomeDestination
import com.seasa.dairy.ui.home.HomeScreen
import com.seasa.dairy.ui.note.NoteDetailsDestination
import com.seasa.dairy.ui.note.NoteDetailsScreen
import com.seasa.dairy.ui.note.NoteEditDestination
import com.seasa.dairy.ui.note.NoteEditScreen
import com.seasa.dairy.ui.note.NoteEntryDestination
import com.seasa.dairy.ui.note.NoteEntryScreen

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun DairyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
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
    }
}
