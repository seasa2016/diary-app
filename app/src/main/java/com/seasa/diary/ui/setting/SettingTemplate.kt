package com.seasa.diary.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsListItem(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // 垂直間距
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 無陰影
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp) // 內容的內邊距
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圖標部分
            Surface(
                modifier = Modifier
                    .size(32.dp), // 圖標背景大小
                shape = MaterialTheme.shapes.small, // 圓角矩形
                color = iconTint // 圖標背景顏色
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // decorative icon
                    tint = Color.White, // 圖標本身的顏色
                    modifier = Modifier.padding(4.dp) // 圖標內邊距
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // 圖標與文字間距

            // 文字部分
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge, // 文字樣式
                modifier = Modifier.weight(1f) // 讓文字佔據剩餘空間
            )

            // 箭頭圖標
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null, // decorative icon
                modifier = Modifier.size(24.dp) // 箭頭大小
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsListItem() {
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = Color.Black)) { // 設定預覽背景色
        Column {
            SettingsListItem(
                icon = Icons.Default.Home, // 替換為您的圖標
                iconTint = Color(0xFFFF9800), // 圖片中「Special features」的橙色
                text = "Special features",
                onClick = { /* Handle click */ }
            )
            SettingsListItem(
                icon = Icons.Default.Settings, // 替換為您的圖標
                iconTint = Color(0xFF4CAF50), // 圖片中「Digital Wellbeing」的綠色
                text = "Digital Wellbeing & parental controls",
                onClick = { /* Handle click */ }
            )
            SettingsListItem(
                icon = Icons.Default.Add, // 替換為您的圖標
                iconTint = Color(0xFF4CAF50), // 圖片中「Additional settings」的綠色
                text = "Additional settings",
                onClick = { /* Handle click */ }
            )
        }
    }
}