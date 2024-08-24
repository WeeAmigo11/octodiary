package org.bxkr.octodiary.screens.navsections.marks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.CloverShape
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.components.MarkComp
import org.bxkr.octodiary.components.MarkConfig
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.events.Mark
import org.bxkr.octodiary.models.marklistsubject.Period
import org.bxkr.octodiary.pxToDp
import kotlin.math.roundToInt

enum class DragValue { Start, Center, End }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectCard(
    period: Period,
    subjectId: Long,
    subjectName: String,
    showRating: Boolean,
    markConfig: MarkConfig,
) {
    val density = LocalDensity.current
    val draggableAnchors = with(density) {
        DraggableAnchors {
            DragValue.Start at 56.dp.toPx()
            DragValue.Center at 0f
            DragValue.End at -56.dp.toPx()
        }
    }
    val anchoredDraggableState = AnchoredDraggableState(
        initialValue = DragValue.Center,
        anchors = draggableAnchors,
        positionalThreshold = { distance -> distance * .5f },
        velocityThreshold = { with(density) { 56.dp.toPx() } },
        animationSpec = tween(500),
        confirmValueChange = {
            if (it == DragValue.Start || it == DragValue.End) {
                modalBottomSheetContentLive.value = { MarkCalculator() }
                modalBottomSheetStateLive.postValue(true)
            }
            false
        }
    )
    val isGlow = subjectId == scrollToSubjectIdLive.value
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    AnimatedContent(targetState = isGlow) { isGlowA ->
        Box {
            Row(
                Modifier
                    .padding(4.dp)
                    .size(size.width.pxToDp(), size.height.pxToDp())
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Calculate,
                    stringResource(R.string.mark_calculator),
                    Modifier.padding(horizontal = 16.dp),
                    MaterialTheme.colorScheme.outline
                )
                Icon(
                    Icons.Rounded.Calculate,
                    stringResource(R.string.mark_calculator),
                    Modifier.padding(horizontal = 16.dp),
                    MaterialTheme.colorScheme.outline
                )
            }
            Card(
                Modifier
                    .padding(4.dp)
                    .offset {
                        IntOffset(
                            x = anchoredDraggableState
                                .requireOffset()
                                .roundToInt(),
                            y = 0
                        )
                    }
                    .onGloballyPositioned { size = it.size }
                    .anchoredDraggable(anchoredDraggableState, Orientation.Horizontal)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = CardDefaults.outlinedCardBorder().copy(
                    3.dp,
                    Brush.linearGradient(
                        0f to MaterialTheme.colorScheme.primary,
                        1f to MaterialTheme.colorScheme.tertiary
                    )
                ).takeIf { isGlowA }
            ) {
                CardContent(period, subjectId, subjectName, showRating, markConfig)
            }
        }
    }
}

@Composable
private fun CardContent(
    period: Period,
    subjectId: Long,
    subjectName: String,
    showRating: Boolean,
    markConfig: MarkConfig,
) {
    Column(Modifier.padding(16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                subjectName,
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                FilterChip(
                    onClick = {},
                    label = {
                        Text(
                            period.value, color = when (period.dynamic) {
                                "UP" -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                            }
                        )
                    },
                    selected = true,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (period.dynamic == "UP") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer),
                    leadingIcon = {
                        if (period.dynamic == "UP") {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropUp,
                                contentDescription = period.dynamic,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = period.dynamic,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp)
                )
                if (period.fixedValue != null) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(period.fixedValue) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Done,
                                contentDescription = stringResource(id = R.string.final_mark),
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            LazyRow(Modifier.weight(1f, fill = true)) {
                items(period.marks) {
                    MarkComp(
                        Mark.fromMarkListSubject(it),
                        subjectId = subjectId,
                        markConfig = markConfig
                    )
                }
            }
            if ((LocalContext.current.mainPrefs.get<Boolean>(CommonPrefs.subjectRating.prefKey) != false) and showRating
            ) {
                DataService.subjectRanking.firstOrNull { it.subjectId == subjectId }
                    ?.let {
                        FilledIconButton(onClick = {
                            modalBottomSheetStateLive.postValue(true)
                            modalBottomSheetContentLive.postValue {
                                SubjectRatingBottomSheet(
                                    subjectId,
                                    subjectName
                                )
                            }
                        }, shape = CloverShape, modifier = Modifier) {

                            Text(
                                it.rank.rankPlace.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
            }
        }
    }
}