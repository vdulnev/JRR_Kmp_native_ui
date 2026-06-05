@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.TabColors
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrDark
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrOnSurface
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrSurface

/** Shared button colours: dark pill + light text idle, gold pill + black text on focus. */
@Composable
fun jrrButtonColors(): ButtonColors = ButtonDefaults.colors(
    containerColor = JrrSurface,
    contentColor = JrrOnSurface,
    focusedContainerColor = JrrGold,
    focusedContentColor = JrrDark,
    pressedContainerColor = JrrGold,
    pressedContentColor = JrrDark,
    disabledContainerColor = JrrSurface,
    disabledContentColor = JrrMuted,
)

/**
 * Collapsible search/filter. Shows a small search-icon button; pressing it opens
 * an inline text field (auto-focused, raises the IME). Dismissal is the TV-native
 * **Back** button, which collapses the field and clears the filter.
 */
@Composable
fun TvSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    // Invoked when the bar opens/closes. Toggling swaps the focused node
    // (button ↔ field); [onToggle] lets the host suppress the resulting focus
    // bounce so it can't hijack the tab row (jumping to another screen).
    onToggle: () -> Unit = {},
) {
    var open by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (open) {
        BackHandler {
            onToggle()
            onQueryChange("")
            open = false
        }
        TvTextField(
            label = hint,
            value = query,
            onValueChange = onQueryChange,
            modifier = modifier.focusRequester(focusRequester),
        )
        LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    } else {
        Button(
            onClick = { onToggle(); open = true },
            colors = jrrButtonColors(),
            modifier = modifier,
        ) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = hint)
        }
    }
}

/**
 * Tab content colours paired with a gold pill indicator: dark text on the
 * selected/focused (gold) tab, light text on the rest. Pair with
 * [jrrPillIndicatorColors] on the TabRow indicator.
 */
@Composable
fun jrrTabColors(): TabColors = TabDefaults.underlinedIndicatorTabColors(
    // Text is always light/gold on the dark bar so it stays readable whether or
    // not the tab row has focus (a gold underline marks the selected tab). This
    // avoids the dark-text-on-unfocused-pill invisibility.
    contentColor = JrrOnSurface,
    inactiveContentColor = JrrMuted,
    selectedContentColor = JrrGold,
    focusedContentColor = JrrGold,
    focusedSelectedContentColor = JrrGold,
)

/** Gold underline/indicator under the selected tab, focused or not. */
val jrrPillActiveColor: androidx.compose.ui.graphics.Color get() = JrrGold
val jrrPillInactiveColor: androidx.compose.ui.graphics.Color get() = JrrGold

/** Square artwork; falls back to a tinted album glyph when no URL is available. */
@Composable
fun TvArtwork(
    url: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (url.isNullOrEmpty()) {
        Box(
            modifier = modifier.clip(shape).background(JrrSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Album,
                contentDescription = null,
                tint = JrrMuted,
                modifier = Modifier.size(48.dp),
            )
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape).background(JrrSurface),
        )
    }
}

/** Album-style poster card: square artwork above a title + subtitle. */
@Composable
fun TvAlbumCard(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 200.dp,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.colors(
            containerColor = JrrSurface,
            contentColor = JrrOnSurface,
            focusedContainerColor = JrrSurface,
            focusedContentColor = JrrOnSurface,
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(3.dp, JrrGold),
                shape = RoundedCornerShape(8.dp),
            ),
        ),
        modifier = modifier,
    ) {
        TvArtwork(
            url = artworkUrl,
            modifier = Modifier.size(width),
            cornerRadius = 0.dp,
        )
        Text(
            text = title.ifEmpty { "Unknown Album" },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 6.dp),
        )
        Text(
            text = subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = JrrMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 8.dp),
        )
    }
}

/**
 * Single-line selectable row rendered as a filled pill: dark surface with light
 * text idle, gold with black text when focused — so lists read as real UI and
 * the focused row is obvious from across the room.
 */
@Composable
fun TvListRow(
    headline: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = JrrSurface,
            contentColor = JrrOnSurface,
            focusedContainerColor = JrrGold,
            focusedContentColor = JrrDark,
            pressedContainerColor = JrrGold,
            pressedContentColor = JrrDark,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = headline.ifEmpty { "Unknown" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

// Loading/empty states are focusable so that while a drill-down screen is
// fetching (no list items yet), focus stays parked in the content area instead
// of bouncing up to the tab strip — which, with focus-to-switch tabs, would
// otherwise hijack the tab and tear down the screen mid-load.
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().focusable(), contentAlignment = Alignment.Center) {
        Text("Loading…", color = JrrGold)
    }
}

@Composable
fun EmptyBox(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().focusable(), contentAlignment = Alignment.Center) {
        Text(message, color = JrrMuted)
    }
}
