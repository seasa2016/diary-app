package com.seasa.diary.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seasa.diary.R
import com.seasa.diary.ui.AppViewModelProvider
import com.seasa.diary.ui.navigation.NavigationDestination


object BackupDestination : NavigationDestination {
    override val route = "backup"
    override val titleRes = R.string.backup
}

@Composable
fun BackupScreen(
    backupViewModel: BackupViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSignInClick: () -> Unit
) {
    val loginState by backupViewModel.loginState.collectAsState() // 觀察 Drive 服務的狀態

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (loginState) {
            is BackupViewModel.LoginState.Success -> {
                val account = (loginState as BackupViewModel.LoginState.Success)

                Text(text = "已登入為：${account.userId}")
                Spacer(modifier = Modifier.height(16.dp))
                /*
            if (driveService != null) {
            Text(text = "Google Drive service is ready！")
            Spacer(modifier = Modifier.height(16.dp))
            // 這裡可以放置備份/還原按鈕，當 Drive 服務可用時才顯示
            Button(onClick = { /* TODO: 觸發備份功能 */ }) {
            Text("Backup to Google Drive")
            }
            Button(onClick = { /* TODO: 觸發還原功能 */ }) {
            Text("Recover from Google Drive")
            }
            } else {
            Text(text = "Initialing Google Drive service...")
            }
            */

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