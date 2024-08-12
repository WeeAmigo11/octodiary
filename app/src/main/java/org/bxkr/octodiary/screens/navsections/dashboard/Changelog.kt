package org.bxkr.octodiary.screens.navsections.dashboard

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.BuildConfig
import org.bxkr.octodiary.R
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.save

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChangelogCard(context: Context) {
    val background = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )
    )
    val mainPrefs = context.mainPrefs
    var shown by remember {
        mutableStateOf(
            (mainPrefs.get<Int>("read_changelog_version") ?: 24) < BuildConfig.VERSION_CODE
        )
    }
    AnimatedVisibility(shown) {
        Row(
            Modifier
                .background(
                    background,
                    MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = {
                        mainPrefs.save("read_changelog_version" to BuildConfig.VERSION_CODE)
                        shown = false
                    },
                    onClick = {
                        mainPrefs.save("read_changelog_version" to BuildConfig.VERSION_CODE)
                        shown = false
                    }
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        stringResource(R.string.changelog),
                        Modifier.padding(end = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "v${BuildConfig.VERSION_NAME}",
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .padding(vertical = 4.dp)
                            .background(DividerDefaults.color, shape = CircleShape)
                    )
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(stringResource(R.string.changelog_text_25))
                    }
                }
                Text(stringResource(R.string.click_to_show_more))
            }
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                stringResource(R.string.next),
                Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}