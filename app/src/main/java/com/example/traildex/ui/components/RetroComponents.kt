package com.example.traildex.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traildex.theme.*

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite,
    borderColor: Color = CharcoalOutline,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .drawBehind {
                if (shadowOffset > 0.dp) {
                    drawRoundRect(
                        color = borderColor,
                        topLeft = androidx.compose.ui.geometry.Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                    )
                }
            }
            .border(2.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .padding(12.dp),
        content = content
    )
}

@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PistachioGreen,
    contentColor: Color = CharcoalText,
    borderColor: Color = CharcoalOutline,
    icon: String? = null,
    subtext: String? = null,
    cornerRadius: Dp = 4.dp,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(cornerRadius)

    val offsetAnim by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        label = "press_offset"
    )
    val shadowAnim by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        label = "press_shadow"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    topLeft = androidx.compose.ui.geometry.Offset(shadowAnim.toPx(), shadowAnim.toPx()),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                )
            }
            .offset(offsetAnim, offsetAnim)
            .border(2.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = text,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        color = contentColor.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun RetroBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SoftTealLight,
    contentColor: Color = CharcoalText,
    borderColor: Color = CharcoalOutline
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .border(1.5.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RetroSegmentedBar(
    totalSegments: Int,
    filledSegments: Int,
    modifier: Modifier = Modifier,
    fillColor: Color = PistachioGreen,
    emptyColor: Color = Color(0xFFD8DBD6),
    borderColor: Color = CharcoalOutline
) {
    Row(
        modifier = modifier
            .height(14.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(2.dp))
            .background(emptyColor, RoundedCornerShape(2.dp))
            .padding(1.5.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until totalSegments) {
            val isFilled = i < filledSegments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isFilled) fillColor else Color.Transparent,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
fun RetroTopBar(
    screenTitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCream)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Telemetry Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(PistachioGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS: OK",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "lllı",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔊 [||||] 94%",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // App & Screen Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                        .background(PistachioGreen, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WD",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = SurfaceWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "WILDDEX V1.0",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalMuted
                    )
                    Text(
                        text = screenTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalText,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Scout Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .border(2.dp, CharcoalOutline, CircleShape)
                    .background(Color(0xFFE2D642), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤠",
                    fontSize = 18.sp
                )
            }
        }
    }
}

data class NavTabItem(val title: String, val icon: String, val index: Int)

@Composable
fun RetroBottomNav(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            NavTabItem("TRAIL", "🧭", 0),
            NavTabItem("CARDS", "🗂️", 1),
            NavTabItem("BATTLE", "⚔️", 2),
            NavTabItem("SCOUT", "🏆", 3)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = CharcoalOutline)
            .background(SurfaceContainer)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = currentTab == tab.index
            val shape = RoundedCornerShape(4.dp)
            val shadowOffset = if (isSelected) 2.dp else 0.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .drawBehind {
                        if (isSelected) {
                            drawRoundRect(
                                color = CharcoalOutline,
                                topLeft = androidx.compose.ui.geometry.Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }
                    }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CharcoalOutline else Color.Transparent,
                        shape = shape
                    )
                    .background(
                        if (isSelected) PistachioGreen else Color.Transparent,
                        shape = shape
                    )
                    .clickable { onTabSelected(tab.index) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.icon,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) CharcoalText else CharcoalMuted
                    )
                }
            }
        }
    }
}
