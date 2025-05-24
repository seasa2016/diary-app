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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {

    private val TAG = "BackupViewModel"

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val WEB_CLIENT_ID = "606059912359-3hhu9qe4e6m76bt9dk7ifgbpb7askt81.apps.googleusercontent.com"

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data class Success(val userId: String, val idToken: String?) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun handleGoogleSignIn(context: Context) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(WEB_CLIENT_ID) // 必須使用你的 Web Client ID
                .setFilterByAuthorizedAccounts(false) // 如果想讓用戶選擇帳戶，設定為 false
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
                _loginState.value = LoginState.Error("登入失敗: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "發生未知錯誤: ${e.message}", e)
                _loginState.value = LoginState.Error("登入失敗: 未知錯誤")
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            val accountId = googleIdTokenCredential.id
                            val displayName = googleIdTokenCredential.displayName
                            Log.d(TAG, "User ID from result: $accountId, Display Name from result: $displayName")

                            _loginState.value = LoginState.Success(accountId, idToken)
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e(TAG, "Failed to parse Google ID Token from result: ${e.message}", e)
                            _loginState.value = LoginState.Error("ID Token 解析失敗 (從結果)")
                        }

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "解析 Google ID Token 失敗: ${e.message}", e)
                        _loginState.value = LoginState.Error("登入失敗: 無法解析 ID Token")
                    } catch (e: Exception) {
                        Log.e(TAG, "處理 Google ID Token 失敗: ${e.message}", e)
                        _loginState.value = LoginState.Error("登入失敗: ID Token 處理錯誤")
                    }
                } else {
                    Log.d(TAG, "處理 Custom Credential: ${credential.type}")
                    // 根據 CustomCredential 的類型處理
                    _loginState.value = LoginState.Error("不支持的憑證類型: ${credential.type}")
                }
            }
            else -> {
                Log.e(TAG, "未知憑證類型: ${credential.type}")
                _loginState.value = LoginState.Error("登入失敗: 未知憑證類型")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _loginState.value = LoginState.Idle
            Log.d(TAG, "用戶已登出。")
        }
    }
}