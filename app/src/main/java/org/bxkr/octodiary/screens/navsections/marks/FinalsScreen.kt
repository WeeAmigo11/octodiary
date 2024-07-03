package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkSimple
import org.bxkr.octodiary.convertToRoman

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FinalsScreen() {
    val subjects = DataService.marksSubject
    val periods = subjects.maxByOrNull { it.periods?.size ?: 0 }?.periods ?: listOf()
    val scrollState = rememberScrollState()
    LazyColumn {
        item {
            Column {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    Row(Modifier.horizontalScroll(scrollState)) {
                        Cell(Modifier.width(256.dp)) {
                            Text(
                                stringResource(R.string.subject),
                                Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(8.dp), style = MaterialTheme.typography.titleMedium
                            )
                        }
                        periods.forEachIndexed { index, period ->
                            Cell {
                                val tooltipState = rememberTooltipState()
                                val coroutineScope = rememberCoroutineScope()
                                TooltipBox(
                                    TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                    tooltip = { PlainTooltip { Text(period.title) } },
                                    tooltipState
                                ) {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                MaterialTheme.shapes.small
                                            )
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                coroutineScope.launch { tooltipState.show() }
                                            }
                                    ) {
                                        Text(
                                            convertToRoman(index + 1),
                                            Modifier.align(Alignment.Center),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                        VerticalDivider(Modifier.height(56.dp))
                        Cell {
                            MarkSimple(stringResource(R.string.year))
                        }
                    }
                }
                HorizontalDivider()
            }
        }
        items(subjects) { subject ->
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Row(Modifier.horizontalScroll(scrollState)) {
                    Cell(Modifier.width(256.dp)) {
                        Text(
                            subject.subjectName,
                            Modifier
                                .align(Alignment.CenterStart)
                                .padding(8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    periods.forEach { period ->
                        Cell {
                            val fixedValue =
                                subject.periods?.firstOrNull { it.title == period.title }?.fixedValue
                            if (fixedValue != null) {
                                MarkSimple(fixedValue)
                            } else {
                                MarkSimple("0", Modifier.alpha(0f))
                            }
                        }
                    }
                    VerticalDivider(Modifier.height(56.dp))
                    Cell {
                        if (subject.yearMark != null) {
                            MarkSimple(subject.yearMark)
                        } else {
                            MarkSimple("0", Modifier.alpha(0f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.padding(8.dp)) {
        content()
    }
}