package org.bxkr.octodiary.screens.navsections.profile.meal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.formatToLongHumanDay
import org.bxkr.octodiary.formatToWeekday
import org.bxkr.octodiary.models.daysbalanceinfo.Day
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.ui.theme.enterTransition
import org.bxkr.octodiary.ui.theme.exitTransition
import kotlin.math.roundToInt

@Composable
fun DiningHistory() {
    with(DataService.daysBalanceInfo) {
        var daysExpanded by remember { mutableStateOf(false) }
        var rotation by remember { mutableFloatStateOf(0f) }
        var countExpandedDays by remember { mutableIntStateOf(10) }

        ElevatedCardWithContent(onClick = {
            daysExpanded = !daysExpanded
            rotation += 180f
        }, title = {
            Text(
                stringResource(R.string.food_history),
                style = MaterialTheme.typography.titleMedium
            )
        }, rotation = rotation) {
            AnimatedVisibility(
                daysExpanded, enter = enterTransition, exit = exitTransition
            ) {
                LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    if (days != null) {
                        itemsIndexed(days) { index, day ->
                            DayContent(day, index, countExpandedDays)
                        }
                        item {
                            val visible = countExpandedDays < days.size

                            AnimatedVisibility(
                                visible = visible,
                                enter = enterTransition,
                                exit = exitTransition
                            ) {
                                Box(
                                    Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            countExpandedDays += 10
                                        }, modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            stringResource(R.string.show_more),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodDayIndicators(day: Day) {
    FoodDayIndicators.values().forEach {
        if (it.condition(day)) {
            Icon(
                it.icon,
                stringResource(it.descriptionRes),
                Modifier.padding(end = 4.dp),
                MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun DayContent(day: Day, index: Int, countExpandedDays: Int) {
    val visible = index + 1 <= countExpandedDays
    var expanded by remember(key1 = day.date) {
        mutableStateOf(
            false
        )
    }
    var dayRotation by remember(key1 = day.date) {
        mutableFloatStateOf(
            0f
        )
    }


    AnimatedVisibility(
        visible, enter = enterTransition, exit = exitTransition
    ) {
        ElevatedCardWithContent(onClick = {
            expanded = !expanded
            dayRotation += 180f
        }, title = {
            Text(
                day.date.parseFromDay()
                    .formatToLongHumanDay(includeYear = true),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                day.date.parseFromDay().formatToWeekday(),
                Modifier
                    .padding(start = 8.dp)
                    .alpha(.8f),
                style = MaterialTheme.typography.titleMedium
            )
        }, icons = {
            FoodDayIndicators(day)
        }, rotation = dayRotation
        ) {
            AnimatedVisibility(
                expanded,
                enter = enterTransition,
                exit = exitTransition
            ) {
                Column(
                    Modifier
                        .padding(
                            horizontal = 16.dp, vertical = 4.dp
                        )
                        .fillMaxWidth()
                ) {
                    day.transactions.forEach { transaction ->
                        var transactionsExpanded by remember(
                            key1 = transaction.type + transaction.sum + day.date
                        ) {
                            mutableStateOf(false)
                        }
                        var transactionsRotation by remember(
                            key1 = transaction.type + transaction.sum + day.date
                        ) {
                            mutableFloatStateOf(0f)
                        }

                        ElevatedCardWithContent(onClick = {
                            transactionsExpanded =
                                !transactionsExpanded
                            transactionsRotation += 180f
                        }, title = {
                            val title =
                                when (transaction.type) {
                                    "DINING" -> stringResource(
                                        id = R.string.dining
                                    )

                                    "BUFFET" -> stringResource(
                                        id = R.string.buffet
                                    )

                                    else -> transaction.type
                                }

                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                (transaction.sum.toFloat() / 100.toFloat()).roundToInt()
                                    .toString() + " ₽",
                                Modifier
                                    .padding(start = 8.dp)
                                    .alpha(.8f)
                            )
                        }, rotation = transactionsRotation
                        ) {
                            AnimatedVisibility(
                                transactionsExpanded,
                                enter = enterTransition,
                                exit = exitTransition
                            ) {
                                Column(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    transaction.items.forEach { item ->
                                        if (item.dishes.isNotEmpty()) {
                                            item.dishes.forEach { dish ->
                                                Row(
                                                    Modifier
                                                        .padding(
                                                            vertical = 4.dp
                                                        )
                                                        .fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        dish.title,
                                                        Modifier
                                                            .weight(
                                                                1f,
                                                                false
                                                            )
                                                            .animateContentSize(),
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )

                                                    if (dish.price != null && dish.price > 0) {
                                                        Text(
                                                            (dish.price / 100.00).toString() + " ₽",
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 8.dp
                                                                )
                                                                .alpha(
                                                                    .8f
                                                                ),
                                                        )
                                                    }

                                                    if (dish.amount != null && dish.amount > 1) {
                                                        Text(
                                                            dish.amount.toString() + "x",
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 8.dp
                                                                )
                                                                .alpha(
                                                                    .8f
                                                                ),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}