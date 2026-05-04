package com.example.translation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical

fun Modifier.pageScrollModifiers(
    scrollBehavior: ScrollBehavior,
): Modifier = this
    .overScrollVertical()
    .nestedScroll(scrollBehavior.nestedScrollConnection)
    .fillMaxHeight()

@Composable
fun pageContentPadding(
    innerPadding: PaddingValues,
    outerPadding: PaddingValues,
    extraTop: Dp = 0.dp,
    extraBottom: Dp = 16.dp,
): PaddingValues {
    val topPadding = innerPadding.calculateTopPadding() + extraTop
    val bottomPadding = outerPadding.calculateBottomPadding() + extraBottom
    return remember(topPadding, bottomPadding) {
        PaddingValues(
            top = topPadding,
            bottom = bottomPadding,
        )
    }
}
