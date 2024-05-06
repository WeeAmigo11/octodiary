package org.bxkr.octodiary.screens.navsections.daybook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarBar(onDaySelect: (Date) -> Unit = {}) {
    val date = remember { Date() }
    val calendar = remember {
        Calendar.getInstance().apply {
            time = date
        }
    }
    val daySelected = daySelectedLive.observeAsState(Date())
    LaunchedEffect(daySelected.value) {
        onDaySelect(daySelected.value)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(
            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            MaterialTheme.shapes.medium.copy(
                topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)
            )
        )
    ) {
        CalendarRow(
            date = calendar,
            daySelected,
            { daySelectedLive.postValue(it) },
            Modifier
                .padding(bottom = 16.dp)
        )
    }
}