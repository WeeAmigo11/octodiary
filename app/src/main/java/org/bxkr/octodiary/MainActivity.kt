package org.bxkr.octodiary

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.bxkr.octodiary.screens.CallbackScreen
import org.bxkr.octodiary.screens.LoginScreen
import org.bxkr.octodiary.screens.NavScreen
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme

val modalBottomSheetStateLive = MutableLiveData(false)
val modalBottomSheetContentLive = MutableLiveData<@Composable () -> Unit> {}
val snackbarHostStateLive = MutableLiveData(SnackbarHostState())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OctoDiaryTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MyApp(
        modifier: Modifier = Modifier
    ) {
        var title by rememberSaveable { mutableIntStateOf(R.string.app_name) }
        val currentScreen = remember {
            mutableStateOf(
                if (authPrefs.get<Boolean>("auth") == true) {
                    Screen.MainNav
                } else Screen.Login
            )
        }
        val showBottomSheet by modalBottomSheetStateLive.observeAsState()
        val bottomSheetContent by modalBottomSheetContentLive.observeAsState()
        val sheetState = rememberModalBottomSheetState()
        val snackbarHostState = snackbarHostStateLive.value!!

        val intentData = intent.dataString
        if (intentData != null && authPrefs.get<Boolean>("auth") != true) {
            currentScreen.value = Screen.Callback
        }
        Scaffold(
            modifier,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(stringResource(title))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = when (currentScreen.value) {
                                Screen.MainNav -> {
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                                }

                                else -> {
                                    MaterialTheme.colorScheme.surface
                                }
                            }
                        )
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Surface {
                title = when (currentScreen.value) {
                    Screen.Login -> {
                        LoginScreen(Modifier.padding(padding))
                        R.string.log_in
                    }

                    Screen.Callback -> {
                        CallbackScreen(
                            Uri.parse(intentData).getQueryParameter("code")!!,
                            currentScreen
                        )
                        R.string.log_in
                    }

                    Screen.MainNav -> {
                        NavScreen(Modifier.padding(padding), currentScreen)
                        R.string.diary
                    }
                }
            }
            if (showBottomSheet == true) {
                ModalBottomSheet(
                    onDismissRequest = { modalBottomSheetStateLive.postValue(false) },
                    sheetState = sheetState
                ) {
                    bottomSheetContent?.invoke()
                }
            }
        }
    }

    @Preview(
        name = "Not Night",
        locale = "ru"
    )
    @Preview(
        name = "Night",
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        locale = "ru"
    )
    @Composable
    fun AppPreview() {
        OctoDiaryTheme {
            MyApp()
        }
    }
}