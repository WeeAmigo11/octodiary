package org.bxkr.octodiary.components.changelog

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import org.bxkr.octodiary.R

class Changelog26 : Changelog() {
    override val versionName: Int
        get() = R.string.c26_version_name
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
                .padding(horizontal = 16.dp), Alignment.Center) {
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

    @Composable
    private fun Finals() {

    }

    object ImageLinks {
        const val NEW_DAYBOOK_IMAGE =
            "https://raw.githubusercontent.com/OctoDiary/.github/master/assets/c26_newdaybook_image.png"
    }
}