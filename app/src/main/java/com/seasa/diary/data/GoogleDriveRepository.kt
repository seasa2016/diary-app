package com.seasa.diary.data

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GoogleDriveRepository(private val context: Context) {
    private var driveService: Drive? = null

    fun initializeDriveService(accountName: String): Boolean {
        return try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            ).apply {
                selectedAccountName = accountName
            }

            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("YourAppName")
                .build()

            true
        } catch (e: Exception) {
            Log.e("DriveService", "Failed to initialize", e)
            false
        }
    }

    suspend fun uploadDatabaseBackup(jsonData: String): BackupResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val fileMetadata = File().apply {
                name = "database_backup_${System.currentTimeMillis()}.json"
                parents = listOf("appDataFolder")
            }

            val mediaContent = ByteArrayContent(
                "application/json",
                jsonData.toByteArray()
            )

            driveService?.files()?.create(fileMetadata, mediaContent)
                ?.setFields("id")
                ?.execute()

            Log.d("DriveUpload", "Backup uploaded successfully")
            BackupResult.Success("Backup uploaded successfully")

        } catch (e: Exception) {
            Log.e("DriveUpload", "Upload failed", e)
            BackupResult.Error("Upload failed: ${e.message}")
        }
    }

    suspend fun downloadLatestBackup(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            // List files in appDataFolder
            val result = driveService?.files()?.list()
                ?.setSpaces("appDataFolder")
                ?.setOrderBy("createdTime desc")
                ?.setPageSize(1)
                ?.setFields("files(id,name,createdTime)")
                ?.execute()

            val files = result?.files
            if (files?.isNotEmpty() == true) {
                val fileId = files[0].id
                Log.d("DriveDownload", "Found backup file: ${files[0].name}")

                // Download file content
                val outputStream = ByteArrayOutputStream()
                driveService?.files()?.get(fileId)
                    ?.executeMediaAndDownloadTo(outputStream)

                val jsonData = outputStream.toString("UTF-8")
                Log.d("DriveDownload", "Downloaded backup successfully")
                jsonData
            } else {
                Log.d("DriveDownload", "No backup files found")
                null
            }
        } catch (e: Exception) {
            Log.e("DriveDownload", "Download failed", e)
            null
        }
    }

    suspend fun listBackups(): List<File>? = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = driveService?.files()?.list()
                ?.setSpaces("appDataFolder")
                ?.setOrderBy("createdTime desc")
                ?.setFields("files(id,name,createdTime)")
                ?.execute()

            result?.files
        } catch (e: Exception) {
            Log.e("DriveList", "Failed to list backups", e)
            null
        }
    }
    /*
    suspend fun deleteBackup(fileId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            driveService?.files()?.delete(fileId)?.execute()
            Log.d("DriveDelete", "Backup deleted successfully")
            true
        } catch (e: Exception) {
            Log.e("DriveDelete", "Delete failed", e)
            false
        }
    }
    */
}

sealed class BackupResult {
    data class Success(val data: Any) : BackupResult()
    data class Error(val message: String) : BackupResult()
}