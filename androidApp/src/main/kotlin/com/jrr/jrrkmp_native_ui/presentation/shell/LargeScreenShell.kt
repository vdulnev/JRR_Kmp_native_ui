package com.jrr.jrrkmp_native_ui.presentation.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.presentation.components.VuMeter
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootConfig
import coil.compose.AsyncImage

private val SIDEBAR_WIDTH = 256.dp

/**
 * Large-screen (tablet) chrome: a persistent left sidebar (brand, nav, docked
 * now-playing cell, active-zone chip) beside the routed [content]. Mirrors the
 * `Tablet & Desktop` design handoff. Navigation stays driven by [RootConfig] via
 * [onSelectTab]; this composable is presentation only.
 */
@Composable
fun LargeScreenShell(
    active: RootConfig,
    onSelectTab: (RootConfig) -> Unit,
    trackName: String?,
    trackArtist: String?,
    trackImageUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    activeZoneName: String,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().background(AppColors.bg1)) {
        Sidebar(
            active = active,
            onSelectTab = onSelectTab,
            trackName = trackName,
            trackArtist = trackArtist,
            trackImageUrl = trackImageUrl,
            isPlaying = isPlaying,
            progress = progress,
            activeZoneName = activeZoneName,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPrevClick = onPrevClick,
        )
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(AppColors.bg1)) {
            content()
        }
    }
}

@Composable
private fun Sidebar(
    active: RootConfig,
    onSelectTab: (RootConfig) -> Unit,
    trackName: String?,
    trackArtist: String?,
    trackImageUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    activeZoneName: String,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(SIDEBAR_WIDTH)
            .fillMaxHeight()
            .background(AppColors.bg2),
    ) {
        // Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AppColors.accent),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "JRIVER",
                    style = AppTypography.monoLabel.copy(
                        color = AppColors.text,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                    ),
                )
                Text(
                    text = "REMOTE",
                    style = AppTypography.monoLabel.copy(
                        color = AppColors.accent,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                    ),
                )
            }
        }

        // Nav
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            NavItem("Now Playing", Icons.Default.PlayArrow, active == RootConfig.Player) { onSelectTab(RootConfig.Player) }
            NavItem("Library", Icons.Default.Home, active == RootConfig.Library) { onSelectTab(RootConfig.Library) }
            NavItem("Zones", Icons.Default.List, active == RootConfig.Zones) { onSelectTab(RootConfig.Zones) }
            NavItem("Settings", Icons.Default.Settings, active == RootConfig.Settings) { onSelectTab(RootConfig.Settings) }
        }

        Spacer(Modifier.weight(1f))

        // Docked now-playing cell
        if (!trackName.isNullOrEmpty()) {
            DockedNowPlaying(
                trackName = trackName,
                trackArtist = trackArtist ?: "",
                trackImageUrl = trackImageUrl,
                isPlaying = isPlaying,
                progress = progress,
                onBodyClick = { onSelectTab(RootConfig.Player) },
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                onPrevClick = onPrevClick,
            )
        }

        // Active zone chip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, AppColors.line, RoundedCornerShape(10.dp))
                .clickable { onSelectTab(RootConfig.Zones) }
                .padding(horizontal = 14.dp, vertical = 11.dp),
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(AppColors.success))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "ACTIVE ZONE",
                    style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 9.sp, letterSpacing = 1.6.sp),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = activeZoneName,
                    style = AppTypography.itemSubtitle.copy(color = AppColors.text, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) AppColors.accent else AppColors.text3
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AppColors.accentDim else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = AppTypography.itemTitle.copy(color = color, fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DockedNowPlaying(
    trackName: String,
    trackArtist: String,
    trackImageUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    onBodyClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.bg3)
            .border(1.dp, AppColors.line2, RoundedCornerShape(10.dp))
            .clickable(onClick = onBodyClick),
    ) {
        // Progress hairline
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(AppColors.accent),
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.bg4),
                ) {
                    if (!trackImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = trackImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trackName,
                        style = AppTypography.itemSubtitle.copy(color = AppColors.text, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = trackArtist,
                        style = AppTypography.itemSubtitle.copy(color = AppColors.text2, fontSize = 11.5.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                VuMeter(isPlaying = isPlaying)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CtlButton(Icons.Default.SkipPrevious, AppColors.text2, 18.dp, onPrevClick)
                    CtlButton(
                        if (isPlaying) Icons.Outlined.Pause else Icons.Default.PlayArrow,
                        AppColors.accent,
                        20.dp,
                        onPlayPauseClick,
                    )
                    CtlButton(Icons.Default.SkipNext, AppColors.text2, 18.dp, onNextClick)
                }
            }
        }
    }
}

@Composable
private fun CtlButton(icon: ImageVector, tint: androidx.compose.ui.graphics.Color, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
    }
}
