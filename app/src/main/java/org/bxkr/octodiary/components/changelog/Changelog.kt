package org.bxkr.octodiary.components.changelog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import org.bxkr.octodiary.BuildConfig

abstract class Changelog {
    abstract val elements: List<ChangelogItem>

    @get:StringRes
    abstract val versionName: Int

    data class ChangelogItem(
        val composable: @Composable () -> Unit,
        @StringRes val title: Int,
        @StringRes val subtitle: Int,
    )

    companion object {
        val currentChangelog: Changelog =
            when (BuildConfig.VERSION_CODE) {
                in 26..Int.MAX_VALUE -> Changelog26()
                else -> Changelog26()
            }
    }
}