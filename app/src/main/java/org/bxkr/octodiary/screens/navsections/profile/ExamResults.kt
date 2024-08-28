package org.bxkr.octodiary.screens.navsections.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.govexams.Exam

@Composable
fun ExamResults() {
    val govExams = remember { DataService.govExams.data }
    val shownCategories = remember {
        mutableStateMapOf(
            *govExams.map {
                when (it.formaGia) {
                    "ЕГЭ" -> Exam.ExamCategories.UnifiedStateExam
                    "ОГЭ" -> Exam.ExamCategories.BasicStateExam
                    else -> Exam.ExamCategories.Other
                }
            }.distinct().associateWith { true }.toList().toTypedArray()
        )
    }

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(stringResource(R.string.exam_results), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.gia_title))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chip = @Composable { it: Map.Entry<Exam.ExamCategories, Boolean> ->
                FilterChip(it.value, { shownCategories[it.key] = !it.value }, {
                    Text(
                        stringResource(
                            when (it.key) {
                                Exam.ExamCategories.UnifiedStateExam -> R.string.ege
                                Exam.ExamCategories.BasicStateExam -> R.string.oge
                                Exam.ExamCategories.Other -> R.string.other
                            }
                        )
                    )
                }, leadingIcon = {
                    AnimatedVisibility(it.value) {
                        Icon(
                            imageVector = Icons.Rounded.Done,
                            contentDescription = stringResource(R.string.select),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                })
            }
            shownCategories.filter { it.key != Exam.ExamCategories.Other }.forEach {
                chip(it)
            }
            if (Exam.ExamCategories.Other in shownCategories) {
                chip(shownCategories.entries.first { it.key == Exam.ExamCategories.Other })
            }
        }

        LazyColumn(
            Modifier.clip(MaterialTheme.shapes.large),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(govExams) { exam ->
                AnimatedVisibility(shownCategories[exam.examCategory] ?: false) {
                    Card(
                        shape = MaterialTheme.shapes.extraSmall,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(exam.name)
                            Text(exam.formaGia)
                        }
                    }
                }
            }
        }
    }
}