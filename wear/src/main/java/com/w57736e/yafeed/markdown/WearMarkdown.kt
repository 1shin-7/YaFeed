package com.w57736e.yafeed.markdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.model.rememberMarkdownState
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import org.intellij.markdown.MarkdownElementTypes.HTML_BLOCK
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun WearMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    state: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 24.dp),
) {
    val markdownState = rememberMarkdownState(content, flavour = GFMFlavourDescriptor(), parser = MarkdownParser(
        GFMFlavourDescriptor()))

    Markdown(
        markdownState = markdownState,
        colors = wearMarkdownColor(),
        typography = wearMarkdownTypography(),
        components = markdownComponents(
            checkbox = { model ->
                WearMarkdownCheckBox(
                    content = model.content,
                    node = model.node,
                    style = model.typography.text
                )
            },
            custom = { elementType, model ->
                when (elementType) {
                    HTML_BLOCK -> {
                        val htmlContent = model.node.getUnescapedTextInNode(model.content)
                        WearHtmlRenderer(
                            htmlContent = htmlContent,
                            typography = model.typography
                        )
                    }
                }
            }
        ),
        success = { successState, components, _ ->
            val nodes = remember(successState.node) { successState.node.children }
            TransformingLazyColumn(
                modifier = modifier,
                state = state,
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    start = contentPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = contentPadding.calculateRightPadding(LayoutDirection.Ltr),
                    bottom = 64.dp
                )
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
