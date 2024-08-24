package org.bxkr.octodiary.components.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.bxkr.octodiary.CachePrefs
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.cachePrefs
import org.bxkr.octodiary.get
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.modalDialogCloseListenerLive
import org.bxkr.octodiary.modalDialogContentLive
import org.bxkr.octodiary.modalDialogStateLive
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classmembers.OctoClassMembers
import org.bxkr.octodiary.models.classmembers.User
import org.bxkr.octodiary.save
import org.bxkr.octodiary.screens.navsections.dashboard.RankingList

private val infoRecomposeTrigger = MutableLiveData(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassInfo() {
    val trigger by infoRecomposeTrigger.observeAsState()
    var showRanking by remember { mutableStateOf(false) }
    val enterTransition1 = remember {
        slideInHorizontally(
            tween(200)
        ) { it }
    }
    val exitTransition1 = remember {
        slideOutHorizontally(
            tween(200)
        ) { it }
    }
    val enterTransition2 = remember {
        slideInHorizontally(
            tween(200)
        ) { -it }
    }
    val exitTransition2 = remember {
        slideOutHorizontally(
            tween(200)
        ) { -it }
    }
    Box {
        AnimatedVisibility(
            visible = showRanking, enter = enterTransition1, exit = exitTransition1
        ) {
            Column {
                IconButton(onClick = { showRanking = false }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(id = R.string.back))
                }
                RankingList()
            }
        }
        AnimatedVisibility(
            visible = !showRanking, enter = enterTransition2, exit = exitTransition2
        ) {
            Column(Modifier.padding(16.dp)) {
                key(trigger) {
                    with(DataService) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(
                                        R.string.class_t, profile.children[currentProfile].className
                                    ), style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    LocalContext.current.resources.getQuantityString(
                                        R.plurals.student_count,
                                        classMembers.size,
                                        classMembers.size
                                    )
                                )
                            }
                            if (!LocalContext.current.isDemo) OutlinedButton({
                                modalDialogContentLive.value = {
                                    AddStudentDialog()
                                }
                                modalDialogCloseListenerLive.value = { }
                                modalDialogStateLive.value = true
                            }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                                Icon(
                                    Icons.Rounded.Add,
                                    stringResource(id = R.string.image),
                                    Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(R.string.add_student))
                            }
                        }
                        Row(
                            Modifier
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(R.string.name_column),
                                Modifier.alpha(.8f),
                                style = MaterialTheme.typography.labelLarge
                            )
                            if (LocalContext.current.mainPrefs.get<Boolean>("main_rating") != false) {
                                Text(
                                    stringResource(R.string.rating_column),
                                    Modifier.alpha(.8f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        LazyColumn(
                            Modifier.clip(MaterialTheme.shapes.large),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(classMembers) {
                                var showDropdown by remember { mutableStateOf(false) }
                                Box {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        ), shape = MaterialTheme.shapes.extraSmall
                                    ) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(onLongClick = {
                                                    showDropdown = true
                                                }) { showDropdown = false }
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    it.user.lastName,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(it.user.run { "$firstName ${middleName ?: ""}" })
                                            }
                                            if (LocalContext.current.mainPrefs.get<Boolean>("main_rating") != false) {
                                                FilledTonalIconButton(
                                                    onClick = { showRanking = true },
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Text(
                                                        ranking.firstOrNull { rankingMember -> it.personId == rankingMember.personId }?.rank?.rankPlace?.toString()
                                                            ?: "?",
                                                        style = MaterialTheme.typography.labelLarge
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    val context = LocalContext.current
                                    DropdownMenu(
                                        showDropdown && it.isCustom,
                                        { showDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            { Text(stringResource(R.string.delete)) },
                                            {
                                                it.personId?.let { it1 ->
                                                    deleteStudent(
                                                        it1,
                                                        context.cachePrefs
                                                    )
                                                }
                                            })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddStudentDialog() {
    var lastName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var personId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    AlertDialog({
        modalDialogStateLive.value = false
        modalDialogContentLive.value = {}
    }, confirmButton = {
        TextButton({
            if (false !in listOf(
                    lastName,
                    firstName,
                    middleName,
                    personId
                ).map { it.isNotBlank() }
            ) {
                isLoading = true
                addNewStudent(
                    User(firstName, lastName, middleName),
                    personId, context.cachePrefs
                ) {
                    isLoading = false
                    modalDialogStateLive.value = false
                    modalDialogContentLive.value = {}
                }
            }
        }, enabled = !isLoading) {
            Text(stringResource(R.string.add))
        }
    }, title = {
        Text(
            stringResource(R.string.new_student)
        )
    }, text = {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
            Column {
                val clipboardManager = LocalClipboardManager.current
                val getCopiedString = { clipboardManager.getText()?.toString() ?: "" }
                textField(lastName, { lastName = it }, stringResource(R.string.last_name))
                textField(firstName, { firstName = it }, stringResource(R.string.first_name))
                textField(middleName, { middleName = it }, stringResource(R.string.middle_name))
                textField(
                    personId,
                    { personId = it },
                    stringResource(R.string.student_id),
                    helpCopy = true,
                    onHelpCopyClick = { personId = getCopiedString() })
            }
        }
    }, icon = {
        AnimatedVisibility(!isLoading) {
            val tooltipState = rememberTooltipState(isPersistent = true)
            val coroutineScope = rememberCoroutineScope()
            TooltipBox(
                TooltipDefaults.rememberPlainTooltipPositionProvider(),
                {
                    PlainTooltip {
                        Text(
                            stringResource(
                                R.string.add_students_help_description,
                                stringResource(DataService.subsystem.title)
                            )
                        )
                    }
                },
                tooltipState
            ) {
                IconButton({
                    coroutineScope.launch {
                        tooltipState.show()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.HelpOutline,
                        stringResource(R.string.what_is_it),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        AnimatedVisibility(isLoading) {
            CircularProgressIndicator()
        }
    })
}

@Composable
private fun textField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    helpCopy: Boolean = false,
    onHelpCopyClick: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Next,
) {
    OutlinedTextField(
        value,
        onValueChange,
        label = { Text(hint) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        trailingIcon = {
            if (helpCopy) IconButton(onClick = onHelpCopyClick) {
                Icon(
                    Icons.Rounded.ContentPaste,
                    stringResource(R.string.paste)
                )
            }
        }
    )
}

private fun addNewStudent(
    names: User,
    personId: String,
    cachePrefs: CachePrefs,
    onUpdated: () -> Unit,
) {
    val newMember = ClassMember(
        personId, names, true
    )
    val newClassMembers = DataService.classMembers + listOf(newMember)
    DataService.classMembers = newClassMembers
    cachePrefs.save("classMembers" to newClassMembers.let { Gson().toJson(it) })
    DataService.pushUserSettings("od_class_members", OctoClassMembers(DataService.classMembers)) {
        infoRecomposeTrigger.postValue(infoRecomposeTrigger.value?.not())
        onUpdated()
    }
}

private fun deleteStudent(
    personId: String,
    cachePrefs: CachePrefs,
) {
    val newClassMembers = DataService.classMembers.filter { it.personId != personId }
    DataService.classMembers = newClassMembers
    cachePrefs.save("classMembers" to newClassMembers.let { Gson().toJson(it) })
    DataService.pushUserSettings("od_class_members", OctoClassMembers(DataService.classMembers)) {
        infoRecomposeTrigger.postValue(infoRecomposeTrigger.value?.not())
    }
}