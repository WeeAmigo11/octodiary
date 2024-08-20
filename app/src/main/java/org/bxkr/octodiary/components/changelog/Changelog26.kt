package org.bxkr.octodiary.components.changelog

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import org.bxkr.octodiary.R
import org.bxkr.octodiary.getDemoField
import org.bxkr.octodiary.pxToDp
import org.bxkr.octodiary.screens.navsections.marks.FinalsScreen
import org.bxkr.octodiary.ui.theme.OctoDiaryTheme
import org.bxkr.octodiary.ui.theme.blue.DarkColorScheme
import org.bxkr.octodiary.ui.theme.blue.LightColorScheme
import kotlin.math.roundToInt

class Changelog26 : Changelog() {
    override val versionName: Int
        get() = R.string.c26_version_name
    override val versionShortname: Int
        get() = R.string.c26_version_shortname
    override val shortDescription: Int
        get() = R.string.c26_description
    override val elements = listOf(
        ChangelogItem(
            @Composable { NewDaybook() },
            R.string.c26_newdaybook_title,
            R.string.c26_newdaybook_subtitle
        ),
        ChangelogItem(
            @Composable { Finals() },
            R.string.c26_finals_title,
            R.string.c26_finals_subtitle
        ),
        ChangelogItem(
            @Composable { Finals() },
            R.string.c26_finals_title,
            R.string.c26_finals_subtitle
        )
    )

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun NewDaybook() {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), Alignment.Center
        ) {
            GlideImage(
                Uri.parse(ImageLinks.NEW_DAYBOOK_IMAGE),
                contentDescription = stringResource(R.string.c26_newdaybook_title),
                loading = placeholder {
                    CircularProgressIndicator()
                },
                transition = CrossFade
            )
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    private fun Finals() {
        var frameSize by remember { mutableStateOf<IntSize?>(null) }
        var contentSize by remember { mutableStateOf<IntSize?>(null) }
        Box(
            Modifier
                .fillMaxSize(), Alignment.Center
        ) {
            GlideImage(
                Uri.parse(ImageLinks.FINALS_FRAME),
                contentDescription = stringResource(R.string.c26_newdaybook_title),
                Modifier.onSizeChanged { size ->
                    frameSize = size
                },
                transition = CrossFade
            )
            val sSize = frameSize
            if (sSize != null) {
                val context = LocalContext.current
                OctoDiaryTheme(
                    darkTheme = true,
                    dynamicColor = false,
                    LightColorScheme,
                    DarkColorScheme,
                    portable = true
                ) {
                    val scale = if (contentSize != null) {
                        val temp =
                            ((frameSize!!.width.toFloat() / contentSize!!.width.toFloat()) * 0.8223872f)
                        if (temp != 0f) temp else 1f
                    } else 1f
                    val temp = frameSize!!.width.toFloat() / 1751f
                    val imageScale = if (temp != 0f) temp else 1f
                    println(scale)
                    println(imageScale)
                    Box(
                        Modifier
                            .padding(
                                top = (55 * imageScale / scale)
                                    .roundToInt()
                                    .pxToDp()
                            )
                            .height(
                                (1968 * imageScale / scale)
                                    .roundToInt()
                                    .pxToDp()
                            )
                            .scale(scale)
                            .onSizeChanged { size ->
                                contentSize = size
                            }
                    ) {
                        FinalsScreen(context.getDemoField(R.raw.demo_marks_subject))
                    }
                }
            }
        }
    }

    object ImageLinks {
        const val NEW_DAYBOOK_IMAGE =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_newdaybook_image.png"
        const val FINALS_FRAME =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_finals_frame.png"
    }
}