package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*

private val SPLIT_SECTIONS_SHAPE = RoundedCornerShape(4.dp)
private val MIN_HEIGHT = 52.dp
private val SPLIT_MIN_WIDTH = 52.dp

@Composable
fun SplitActionButton(
    label: String,
    onContainerClick: () -> Unit,
    onActionClick: () -> Unit,
    actionIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RadioButtonDefaults.splitRadioButtonShape,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val actionContainerColor = MaterialTheme.colorScheme.error
    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = MIN_HEIGHT)
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Max)
            .graphicsLayer {
                this.shape = shape
                clip = true
            },
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    enabled = enabled,
                    onClick = onContainerClick,
                )
                .semantics { role = Role.Button }
                .fillMaxHeight()
                .clip(SPLIT_SECTIONS_SHAPE)
                .drawBehind { drawRect(containerColor) }
                .padding(RadioButtonDefaults.ContentPadding)
                .weight(1.0f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                color = contentColor
            )
        }

        Spacer(modifier = Modifier.size(2.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clickable(
                    enabled = enabled,
                    onClick = onActionClick,
                )
                .fillMaxHeight()
                .clip(SPLIT_SECTIONS_SHAPE)
                .drawBehind { drawRect(actionContainerColor) }
                .defaultMinSize(minWidth = SPLIT_MIN_WIDTH)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(RadioButtonDefaults.ContentPadding)
        ) {
            actionIcon()
        }
    }
}
