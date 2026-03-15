package com.w57736e.yafeed.markdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.model.rememberMarkdownState

@Composable
fun WearMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 24.dp),
) {
    val markdownState = rememberMarkdownState(content)

    Markdown(
        markdownState = markdownState,
        colors = wearMarkdownColor(),
        typography = wearMarkdownTypography(),
        success = { successState, components, _ ->
            val nodes = remember(successState.node) { successState.node.children }
            ScalingLazyColumn(
                modifier = modifier,
                state = state,
                contentPadding = contentPadding
            ) {
                items(nodes, key = { it.startOffset }) { node ->
                    MarkdownElement(
                        content = successState.content,
                        node = node,
                        components = components
                    )
                }
            }
        }
    )
}
