package org.bxkr.octodiary.screens.navsections.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.models.profile.Children

@Composable
fun ProfileScreen2() {
    val child = DataService.profile.children[DataService.currentProfile]
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ShortProfileInfo(child)
        Cards()
    }
}

@Composable
private fun ShortProfileInfo(child: Children) {
    Row {
        Column(Modifier.padding(16.dp)) {
            Text(
                child.run { "$lastName $firstName $middleName" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(child.school.shortName)
            Text(stringResource(R.string.class_t, child.className))
        }
    }
}

@Composable
private fun Cards() {
    Column(
        Modifier
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ProfileCard(R.string.personal_data, Icons.Rounded.Person) { PersonalData() }
        ProfileCard(R.string.class_label, Icons.Rounded.Group) { ClassInfo() }
        ProfileCard(R.string.school_and_teachers, Icons.Rounded.School) { School() }
        if (isExamsNotEmpty()) ProfileCard(
            R.string.exam_results,
            Icons.Rounded.Grade
        ) { ExamResults() }
        ProfileCard(R.string.meal, Icons.Rounded.Restaurant) { Meal() }
        ProfileCard(R.string.documents, Icons.Rounded.Description) { Documents() }
    }
}

@Composable
private fun ProfileCard(
    @StringRes textRes: Int,
    icon: ImageVector,
    bottomSheetContent: @Composable () -> Unit,
) {
    ProfileCard(stringResource(textRes), icon, bottomSheetContent)
}

@Composable
private fun ProfileCard(
    text: String,
    icon: ImageVector,
    bottomSheetContent: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { openBottomSheet { bottomSheetContent() } }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.padding(end = 8.dp), MaterialTheme.colorScheme.onSurface)
        Text(text)
    }
}

private fun isExamsNotEmpty(): Boolean = DataService.govExams.data.isNotEmpty()

private fun openBottomSheet(content: @Composable () -> Unit) {
    modalBottomSheetStateLive.postValue(true)
    modalBottomSheetContentLive.postValue(content)
}