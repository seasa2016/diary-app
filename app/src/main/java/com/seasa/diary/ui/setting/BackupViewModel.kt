package com.seasa.diary.ui.setting

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.seasa.diary.data.BackupResult
import com.seasa.diary.data.GoogleDriveRepository
import com.seasa.diary.data.LoginRepository
import com.seasa.diary.data.NotesRepository
import com.seasa.diary.ui.setting.BackupViewModel.LoginState.Error
import com.seasa.diary.ui.setting.BackupViewModel.LoginState.Idle
import com.seasa.diary.ui.setting.BackupViewModel.LoginState.Loading
import com.seasa.diary.ui.setting.BackupViewModel.LoginState.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BackupViewModel(
    private val loginRepository: LoginRepository,
    private val driveRepository: GoogleDriveRepository,
    private val notesRepository: NotesRepository,
) : ViewModel() {

    private val TAG = "BackupViewModel"

    private val _loginState = MutableStateFlow<LoginState>(Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val WEB_CLIENT_ID =
        "606059912359-3hhu9qe4e6m76bt9dk7ifgbpb7askt81.apps.googleusercontent.com"

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data class Success(val userId: String, val idToken: String?) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private suspend fun handleSuccessLogin(
        userId: String,
        idToken: String,
        displayName: String,
        updateToRepo: Boolean
    ) {
        _loginState.value = Success(userId, idToken)
        driveRepository.initializeDriveService(userId)

        if (updateToRepo) {
            loginRepository.saveLoginState(true, userId, displayName, idToken)
        }
    }

    private suspend fun handleFailLogin(msg: String, updateToRepo: Boolean) {
        _loginState.value = Error(msg)

        if (updateToRepo) {
            loginRepository.clearLoginState()
        }
    }

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            _loginState.value = Loading // 顯示載入狀態
            val isLoggedIn = loginRepository.checkSavedLoginState()
            if (isLoggedIn) {
                val userId = loginRepository.userId.firstOrNull()
                val idToken = loginRepository.idToken.firstOrNull()
                val displayName = loginRepository.userDisplayName.firstOrNull()

                if (!userId.isNullOrEmpty() && !idToken.isNullOrEmpty() && !displayName.isNullOrEmpty()) {
                    Log.d(TAG, "Found saved login state for user: $userId")

                    handleSuccessLogin(userId, idToken, displayName, false)
                } else {
                    Log.d(TAG, "Saved login state found but user ID is empty. Resetting.")
                    handleFailLogin("Login info updated", true)
                }
            } else {
                Log.d(TAG, "No saved login state found. User needs to log in.")
                handleFailLogin("No saved login info", false)
            }
        }
    }

    fun handleGoogleSignIn(context: Context) {
        _loginState.value = Loading
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(WEB_CLIENT_ID) // 必須使用你的 Web Client ID
                .setFilterByAuthorizedAccounts(false) // 如果想讓用戶選擇帳戶，設定為 false
                .setAutoSelectEnabled(false)
                .setRequestVerifiedPhoneNumber(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    context,
                    request,
                )
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager 獲取憑證失敗: ${e.message}", e)
                _loginState.value = Error("登入失敗: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "發生未知錯誤: ${e.message}", e)
                _loginState.value = Error("登入失敗: 未知錯誤")
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            val accountId = googleIdTokenCredential.id
                            val displayName = googleIdTokenCredential.displayName
                            Log.d(
                                TAG,
                                "User ID from result: $accountId, Display Name from result: $displayName"
                            )

                            if (displayName != null) {
                                handleSuccessLogin(accountId, idToken, displayName, true)
                            }
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(
                                TAG,
                                "Failed to parse Google ID Token from result: ${e.message}",
                                e
                            )
                            handleFailLogin("ID Token 解析失敗 (從結果)", true)
                        }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "解析 Google ID Token 失敗: ${e.message}", e)
                        handleFailLogin("登入失敗: 無法解析 ID Token", true)
                    } catch (e: Exception) {
                        Log.e(TAG, "處理 Google ID Token 失敗: ${e.message}", e)
                        handleFailLogin("登入失敗: ID Token 處理錯誤", true)
                    }
                } else {
                    Log.d(TAG, "處理 Custom Credential: ${credential.type}")
                    // 根據 CustomCredential 的類型處理
                    handleFailLogin("不支持的憑證類型: ${credential.type}", true)
                }
            }

            else -> {
                Log.e(TAG, "未知憑證類型: ${credential.type}")
                handleFailLogin("登入失敗: 未知憑證類型", true)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _loginState.value = Idle
            Log.d(TAG, "用戶已登出。")
            loginRepository.saveLoginState(false, null, null, null)
        }
    }

    private val _driveState = MutableStateFlow(DriveUiState())
    val driveState: StateFlow<DriveUiState> = _driveState.asStateFlow()


    fun uploadBackup() {
        when (_loginState.value) {
            is Idle -> {
                _driveState.value = _driveState.value.copy(
                    message = "Please login first",
                    isError = true
                )
                return
            }

            else -> {}
        }

        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(
                isUploading = true,
                isLoading = true,
                message = "Preparing backup..."
            )

            try {
                // Export database
                _driveState.value = _driveState.value.copy(message = "Exporting database...")
                val jsonData = notesRepository.exportDatabaseToJson()

                // Upload to Drive
                _driveState.value = _driveState.value.copy(message = "Uploading to Google Drive...")
                val result = driveRepository.uploadDatabaseBackup(jsonData)

                when (result) {
                    is BackupResult.Success -> {
                        _driveState.value = _driveState.value.copy(
                            isUploading = false,
                            isLoading = false,
                            message = "Backup uploaded successfully!",
                            isError = false,
                        )
                    }

                    is BackupResult.Error -> {
                        _driveState.value = _driveState.value.copy(
                            isUploading = false,
                            isLoading = false,
                            message = result.message,
                            isError = true,
                        )
                    }
                }
            } catch (e: Exception) {
                _driveState.value = _driveState.value.copy(
                    isUploading = false,
                    isLoading = false,
                    message = "Upload error: ${e.message}",
                    isError = true,
                )
            }
        }
    }

    fun downloadBackup() {
        when (_loginState.value) {
            is Success -> {
            }

            else -> {
                _driveState.value = _driveState.value.copy(
                    message = "Please sign in first",
                    isError = true
                )
                return
            }
        }

        viewModelScope.launch {
            _driveState.value = _driveState.value.copy(
                isDownloading = true,
                isLoading = true,
                message = "Searching for backups..."
            )

            try {
                // Download from Drive
                _driveState.value =
                    _driveState.value.copy(message = "Downloading from Google Drive...")
                val jsonData = driveRepository.downloadLatestBackup()

                if (jsonData != null) {
                    // Import to database
                    _driveState.value = _driveState.value.copy(message = "Restoring database...")
                    notesRepository.importDatabaseFromJson(jsonData)

                    _driveState.value = _driveState.value.copy(
                        isDownloading = false,
                        isLoading = false,
                        message = "Backup restored successfully!",
                        isError = false,
                    )
                } else {
                    _driveState.value = _driveState.value.copy(
                        isDownloading = false,
                        isLoading = false,
                        message = "No backup found in Google Drive",
                        isError = true
                    )
                }
            } catch (e: Exception) {
                _driveState.value = _driveState.value.copy(
                    isDownloading = false,
                    isLoading = false,
                    message = "Download error: ${e.message}",
                    isError = true
                )
            }
        }
    }
    /*
    fun listBackups() {
        if (_loginState.value !is Success) return

        viewModelScope.launch {
            try {
                val backups = driveRepository.listBackups()
                _driveState.value = _driveState.value.copy(
                    availableBackups = backups?.map { file ->
                        BackupInfo(
                            id = file.id,
                            name = file.name,
                            createdTime = file.createdTime?.value ?: 0L
                        )
                    } ?: emptyList()
                )
            } catch (e: Exception) {
                _driveState.value = _driveState.value.copy(
                    message = "Failed to list backups: ${e.message}",
                    isError = true
                )
            }
        }
    }
*/
}

data class BackupInfo(
    val id: String,
    val name: String,
    val createdTime: Long
)

data class DriveUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val availableBackups: List<BackupInfo> = emptyList(),
)
