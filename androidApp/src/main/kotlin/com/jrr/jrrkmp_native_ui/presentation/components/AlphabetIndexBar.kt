package com.jrr.jrrkmp_native_ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import kotlin.math.roundToInt

/**
 * Returns the section bucket letter for an alphabetical list label: the
 * uppercased first character if it's a letter in any script (Latin, Cyrillic,
 * …), otherwise '#' (digits, symbols) so those collapse into a single leading
 * bucket — matching the way the lists are sorted case-insensitively. The bar
 * derives its sections from the live data, so non-Latin letters show up as
 * their own sections in sort order.
 */
fun sectionLetterFor(label: String): Char {
    val first = label.trim().firstOrNull()?.uppercaseChar() ?: return '#'
    return if (first.isLetter()) first else '#'
}

/**
 * A vertical A–Z fast-scroll scrubber pinned to the trailing edge of its
 * parent [androidx.compose.foundation.layout.Box]. Tapping or dragging a
 * letter invokes [onLetterSelected]; the caller maps that to a list index and
 * scrolls. Only renders when there are at least two distinct sections, since a
 * single-bucket list has nothing to jump between.
 *
 * @param letters the distinct section letters, already in display order.
 */
@Composable
fun BoxScope.AlphabetIndexBar(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (letters.size < 2) return

    var barHeightPx by remember { mutableStateOf(0) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var touchY by remember { mutableStateOf(0f) }

    // Decimation: when there are more letters than comfortably fit, render only
    // every `step`-th letter (plus first/last/active) and show '·' for the rest.
    // The gesture still maps across all letters, so every one stays reachable.
    val minLabelPx = with(LocalDensity.current) { 14.dp.toPx() }
    val maxLabels = if (barHeightPx > 0) {
        (barHeightPx / minLabelPx).toInt().coerceAtLeast(1)
    } else {
        letters.size
    }
    val step = if (letters.size <= maxLabels) {
        1
    } else {
        (letters.size + maxLabels - 1) / maxLabels
    }

    fun selectAtY(y: Float) {
        if (barHeightPx <= 0) return
        val idx = ((y / barHeightPx) * letters.size)
            .toInt()
            .coerceIn(0, letters.size - 1)
        val letter = letters[idx]
        if (letter != activeLetter) {
            activeLetter = letter
            onLetterSelected(letter)
        }
    }

    Column(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 8.dp)
            .onSizeChanged { barHeightPx = it.height }
            // One gesture loop handles both a tap (initial down) and a drag,
            // so a letter is selected the instant a finger lands and tracks as
            // it slides. Releasing clears the highlight.
            .pointerInput(letters) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        isDragging = true
                        touchY = down.position.y
                        selectAtY(down.position.y)
                        var event = awaitPointerEvent()
                        while (event.changes.any { it.pressed }) {
                            event.changes.forEach { change ->
                                touchY = change.position.y
                                selectAtY(change.position.y)
                                change.consume()
                            }
                            event = awaitPointerEvent()
                        }
                        isDragging = false
                        activeLetter = null
                    }
                }
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEachIndexed { i, letter ->
            val isActive = letter == activeLetter
            val showLetter =
                step == 1 || i % step == 0 || i == letters.lastIndex || isActive
            androidx.compose.material3.Text(
                text = if (showLetter) letter.toString() else "·",
                textAlign = TextAlign.Center,
                style = AppTypography.chipMono.copy(
                    color = if (isActive) AppColors.accent else AppColors.text3,
                    fontSize = 11.sp,
                ),
            )
        }
    }

    // Magnification bubble tracking the finger, drawn to the left of the strip.
    val current = activeLetter
    if (isDragging && current != null) {
        val density = LocalDensity.current
        val bubbleSizeDp = 56.dp
        val bubbleSizePx = with(density) { bubbleSizeDp.toPx() }
        val topPaddingPx = with(density) { 8.dp.toPx() }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val maxY = (barHeightPx - bubbleSizePx).coerceAtLeast(0f)
                    IntOffset(
                        x = -with(density) { 64.dp.roundToPx() },
                        y = (topPaddingPx + touchY - bubbleSizePx / 2f)
                            .coerceIn(0f, maxY + topPaddingPx)
                            .roundToInt(),
                    )
                }
                .size(bubbleSizeDp)
                .clip(CircleShape)
                .background(AppColors.bg3)
                .border(1.5.dp, AppColors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Text(
                text = current.toString(),
                textAlign = TextAlign.Center,
                style = AppTypography.chipMono.copy(
                    color = AppColors.text,
                    fontSize = 26.sp,
                ),
            )
        }
    }
}
