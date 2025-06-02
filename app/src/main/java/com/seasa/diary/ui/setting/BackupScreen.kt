package com.seasa.diary.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seasa.diary.DiaryTopAppBar
import com.seasa.diary.R
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.navigation.NavigationDestination
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupDestination : NavigationDestination {
    override val route = "backup"
    override val titleRes = R.string.backup
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    backupViewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSignInClick: () -> Unit
) {
    val loginState by backupViewModel.loginState.collectAsState() // 觀察 Drive 服務的狀態
    val driveState by backupViewModel.driveState.collectAsState() // 觀察 Drive 服務的狀態

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiaryTopAppBar(
                title = stringResource(BackupDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (loginState) {
                        is BackupViewModel.LoginState.Success -> {
                            val account = (loginState as BackupViewModel.LoginState.Success)

                            Text(text = "已登入為：${account.userId}")
                            Spacer(modifier = Modifier.height(16.dp))

                            Spacer(modifier = Modifier.height(32.dp))
                            Button(onClick = { backupViewModel.signOut() }) {
                                Text("Logout")
                            }
                        }

                        else -> {
                            Text(text = "Pls login Google account")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick =
                                    onSignInClick
                            ) {
                                Text("Login with Google")
                            }
                        }
                    }
                }
            }
            when (loginState) {
                is BackupViewModel.LoginState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Backup Operations",
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { backupViewModel.uploadBackup() },
                                    enabled = !driveState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (driveState.isUploading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                        )
                                    } else {
                                        Text("Upload Backup")
                                    }
                                }

                                Button(
                                    onClick = { backupViewModel.downloadBackup() },
                                    enabled = !driveState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (driveState.isDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                        )
                                    } else {
                                        Text("Download Backup")
                                    }
                                }
                            }

                            driveState.message?.let { message ->
                                Surface(
                                    color = if (driveState.isError)
                                        MaterialTheme.colorScheme.errorContainer
                                    else
                                        MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = message,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (driveState.isError)
                                            MaterialTheme.colorScheme.onErrorContainer
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if (!driveState.isLoading) {
                                    LaunchedEffect(message) {
                                        kotlinx.coroutines.delay(5000)
                                        //driveState.clearMessage()
                                    }
                                }
                            }
                        }
                    }

                    // Available Backups Section
                    if (driveState.availableBackups.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Available Backups",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(driveState.availableBackups) { backup ->
                                        BackupItem(backup = backup)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun BackupItem(backup: BackupInfo) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = backup.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = dateFormat.format(Date(backup.createdTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
