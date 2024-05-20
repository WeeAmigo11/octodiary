package org.bxkr.octodiary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.bxkr.octodiary.MainActivity
import org.bxkr.octodiary.components.debugutils.PrefEditor

@Composable
fun DebugMenu(
    context: MainActivity,
) {
    var isHide by remember { mutableStateOf(false) }
    var isShownMenu by remember { mutableStateOf(false) }
    var currentFunction: @Composable () -> Unit by remember { mutableStateOf({}) }
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    LaunchedEffect(interactionSource) {
        var isLongClick = false

        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    isLongClick = true
                    isHide = true
                }

                is PressInteraction.Release -> {
                    if (isLongClick.not()) {
                        isShownMenu = !isShownMenu
                    }
                }
            }
        }
    }
    AnimatedVisibility(visible = !isHide, exit = slideOut { IntOffset(0, -it.height * 2) }) {
        OutlinedButton(
            onClick = {},
            Modifier.padding(end = 8.dp),
            interactionSource = interactionSource
        ) {
            Text("Debug")
        }
    }
    DropdownMenu(isShownMenu, onDismissRequest = { isShownMenu = false }) {
        DebugMenuItems.values().forEach {
            DropdownMenuItem(
                { Text(it.title) },
                onClick = { currentFunction = { it.function(context) { currentFunction = {} } } })
        }
    }
    currentFunction()
}

enum class DebugMenuItems(
    val title: String,
    val function: @Composable MainActivity.(clearFn: () -> Unit) -> Unit,
) {
    PreferenceEditor("Preference editor", { clearFn -> PrefEditor { clearFn() } })
}