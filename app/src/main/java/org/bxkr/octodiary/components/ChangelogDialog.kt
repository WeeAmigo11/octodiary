package org.bxkr.octodiary.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.changelog.Changelog

@Composable
fun ChangelogDialog(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest,
        DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ChangelogNavigation(Changelog.currentChangelog, onDismissRequest)
    }
}

@Composable
private fun ChangelogNavigation(currentChangelog: Changelog, onDismissRequest: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    var button1Style by remember { mutableStateOf("exit") }
    var button2Style by remember { mutableStateOf("exit") }
    button1Style = if (currentPage == 0) "exit" else "back"
    button2Style = if (currentPage == currentChangelog.elements.lastIndex) "exit" else "next"

    val onButton1Click = {
        if (currentPage > 0) currentPage -= 1
        else onDismissRequest()
    }

    val onButton2Click = {
        if (currentPage < currentChangelog.elements.lastIndex) currentPage += 1
        else onDismissRequest()
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(
                stringResource(currentChangelog.versionName),
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
            AnimatedContent(
                currentPage,
                Modifier
                    .weight(1f)
                    .fillMaxWidth(), label = "page_anim"
            ) { innerCurrentPage ->
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .weight(1f)
                            .zoomable(rememberZoomableState(ZoomSpec(maxZoomFactor = 2f)))
                    ) { currentChangelog.elements[innerCurrentPage].composable() }
                    Column(
                        Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            stringResource(currentChangelog.elements[innerCurrentPage].title),
                            Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            stringResource(currentChangelog.elements[innerCurrentPage].subtitle),
                            Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center
            ) {
                AnimatedContent(button1Style, label = "controls_anim") { innerStyle ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(144.dp, 48.dp)
                            .border(
                                if (innerStyle == "exit") Dp.Unspecified else 1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.medium
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onButton1Click() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (innerStyle) {
                                    "exit" -> Icons.Rounded.Close
                                    else -> Icons.AutoMirrored.Rounded.ArrowBack
                                },
                                stringResource(R.string.back_exit),
                                Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp),
                                MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(
                                    when (innerStyle) {
                                        "exit" -> R.string.close
                                        else -> R.string.back
                                    }
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                AnimatedContent(button2Style, label = "controls_anim") { innerStyle ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(144.dp, 48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.medium
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onButton2Click() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(
                                    when (innerStyle) {
                                        "exit" -> R.string.close
                                        else -> R.string.next
                                    }
                                ),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                when (innerStyle) {
                                    "exit" -> Icons.Rounded.Close
                                    else -> Icons.AutoMirrored.Rounded.ArrowForward
                                },
                                stringResource(R.string.next),
                                Modifier
                                    .padding(start = 8.dp)
                                    .size(18.dp),
                                MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}