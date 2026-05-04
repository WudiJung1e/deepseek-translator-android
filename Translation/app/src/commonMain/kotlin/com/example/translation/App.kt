package com.example.translation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.example.translation.data.DeepSeekApi
import com.example.translation.data.TranslationHistoryEntry
import com.example.translation.ui.HistoryScreen
import com.example.translation.ui.SettingsScreen
import com.example.translation.ui.TranslatorScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

@Composable
fun App(
    apiKey: String,
    colorMode: Int,
    historyEntries: List<TranslationHistoryEntry>,
    onApiKeyChanged: (String) -> Unit,
    onColorModeChanged: (Int) -> Unit,
    onHistoryEntryAdded: (TranslationHistoryEntry) -> Unit,
    onHistoryCleared: () -> Unit,
) {
    val api = remember(apiKey) { DeepSeekApi(apiKey) }
    val pagerState = rememberPagerState { AppScreen.PageCount }
    val mainPagerState = rememberMainPagerState(pagerState)
    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
    }
    val useDarkTheme = when (colorMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val colors = if (useDarkTheme) {
        darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color.Black,
        )
    } else {
        lightColorScheme()
    }

    MiuixTheme(colors = colors) {
        val surfaceColor = MiuixTheme.colorScheme.surface
        val outerBackdrop: LayerBackdrop? = if (isRenderEffectSupported()) {
            rememberLayerBackdrop {
                drawRect(surfaceColor)
                drawContent()
            }
        } else {
            null
        }
        val outerBlurActive = outerBackdrop != null
        val outerBarColor = if (outerBlurActive) Color.Transparent else MiuixTheme.colorScheme.surface

        Scaffold(
            bottomBar = {
                BlurredBar(backdrop = outerBackdrop, blurEnabled = outerBlurActive) {
                    BottomBar(
                        currentScreen = AppScreen.fromPage(mainPagerState.selectedPage),
                        onScreenSelected = { screen ->
                            mainPagerState.animateToPage(screen.page)
                        },
                        color = outerBarColor,
                    )
                }
            },
        ) { innerPadding ->
            Box(modifier = if (outerBackdrop != null) Modifier.layerBackdrop(outerBackdrop) else Modifier) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier,
                    userScrollEnabled = false,
                ) { page ->
                    val screen = AppScreen.fromPage(page)
                    PageScaffold(
                        title = screen.title,
                        outerPadding = innerPadding,
                    ) { contentPadding, scrollBehavior ->
                        when (screen) {
                            AppScreen.Translator -> TranslatorScreen(
                                api = api,
                                contentPadding = contentPadding,
                                scrollBehavior = scrollBehavior,
                                onTranslationSaved = onHistoryEntryAdded,
                            )
                            AppScreen.History -> HistoryScreen(
                                historyEntries = historyEntries,
                                onHistoryCleared = onHistoryCleared,
                                contentPadding = contentPadding,
                                scrollBehavior = scrollBehavior,
                            )
                            AppScreen.Settings -> SettingsScreen(
                                apiKey = apiKey,
                                colorMode = colorMode,
                                onApiKeyChanged = onApiKeyChanged,
                                onColorModeChanged = onColorModeChanged,
                                contentPadding = contentPadding,
                                scrollBehavior = scrollBehavior,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageScaffold(
    title: String,
    outerPadding: PaddingValues,
    content: @Composable (PaddingValues, ScrollBehavior) -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val pageBackdrop: LayerBackdrop? = if (isRenderEffectSupported()) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else {
        null
    }
    val blurActive = pageBackdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = pageBackdrop, blurEnabled = blurActive) {
                TopAppBar(
                    title = title,
                    scrollBehavior = scrollBehavior,
                    color = barColor,
                )
            }
        },
    ) { innerPadding ->
        val contentPadding = remember(innerPadding, outerPadding) {
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = outerPadding.calculateBottomPadding(),
            )
        }
        Box(modifier = if (pageBackdrop != null) Modifier.layerBackdrop(pageBackdrop) else Modifier) {
            content(contentPadding, scrollBehavior)
        }
    }
}

@Composable
private fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val surfaceColor = MiuixTheme.colorScheme.surface
    Box(
        modifier = if (blurEnabled && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f,
                colors = BlurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = surfaceColor.copy(alpha = 0.8f)),
                    ),
                ),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}

private enum class AppScreen {
    Translator,
    History,
    Settings;

    val title: String
        get() = when (this) {
            Translator -> "DeepSeek 翻译"
            History -> "历史"
            Settings -> "设置"
        }

    val page: Int
        get() = when (this) {
            Translator -> 0
            History -> 1
            Settings -> 2
        }

    companion object {
        const val PageCount = 3

        fun fromPage(page: Int): AppScreen = when (page) {
            0 -> Translator
            1 -> History
            else -> Settings
        }
    }
}

@Stable
private class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.scroll(MutatePriority.UserInput) {
                    val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
                    val duration = 100 * distance + 100
                    val layoutInfo = pagerState.layoutInfo
                    val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
                    val currentDistanceInPages = targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
                    val scrollPixels = currentDistanceInPages * pageSize

                    var previousValue = 0f
                    animate(
                        initialValue = 0f,
                        targetValue = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    ) { currentValue, _ ->
                        previousValue += scrollBy(currentValue - previousValue)
                    }
                }

                if (pagerState.currentPage != targetIndex) {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
private fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState = remember(pagerState, coroutineScope) {
    MainPagerState(pagerState, coroutineScope)
}

@Composable
private fun BottomBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        color = color,
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.Translator,
            onClick = { onScreenSelected(AppScreen.Translator) },
            icon = MiuixIcons.Translate,
            label = "翻译",
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.History,
            onClick = { onScreenSelected(AppScreen.History) },
            icon = MiuixIcons.Recent,
            label = "历史",
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Settings,
            onClick = { onScreenSelected(AppScreen.Settings) },
            icon = MiuixIcons.Settings,
            label = "设置",
        )
    }
}
