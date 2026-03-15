package com.w57736e.yafeed.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

data class ContentBlock(
    val type: BlockType,
    val content: String
)

enum class BlockType {
    TEXT, IMAGE
}

@Composable
fun ContentRenderer(
    content: String,
    textStyle: TextStyle,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showImages: Boolean = true
) {
    val blocks = androidx.compose.runtime.remember(content) { parseContent(content) }
    val context = LocalContext.current

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block.type) {
                BlockType.TEXT -> {
                    if (block.content.isNotBlank()) {
                        Text(
                            text = block.content.trim(),
                            style = textStyle,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                BlockType.IMAGE -> {
                    if (showImages) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(block.content)
                                .size(Size(300, 300))
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onImageClick(block.content) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

private fun parseContent(content: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val imgRegex = Regex("""<img[^>]+src=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE)
    val markdownImgRegex = Regex("""!\[.*?\]\(([^)]+)\)""")

    var remaining = content
    var lastIndex = 0

    // Find all images
    val allMatches = (imgRegex.findAll(content) + markdownImgRegex.findAll(content))
        .sortedBy { it.range.first }

    allMatches.forEach { match ->
        // Add text before image
        if (match.range.first > lastIndex) {
            val text = remaining.substring(lastIndex, match.range.first)
                .replace(Regex("<[^>]+>"), "") // Strip HTML tags
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1") // Strip markdown bold
                .replace(Regex("\\*(.+?)\\*"), "$1") // Strip markdown italic
            if (text.isNotBlank()) {
                blocks.add(ContentBlock(BlockType.TEXT, text))
            }
        }

        // Add image
        val imageUrl = match.groupValues[1]
        blocks.add(ContentBlock(BlockType.IMAGE, imageUrl))

        lastIndex = match.range.last + 1
    }

    // Add remaining text
    if (lastIndex < remaining.length) {
        val text = remaining.substring(lastIndex)
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
        if (text.isNotBlank()) {
            blocks.add(ContentBlock(BlockType.TEXT, text))
        }
    }

    // If no blocks, add all as text
    if (blocks.isEmpty()) {
        val cleanText = content
            .replace(Regex("<[^>]+>"), "")
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("\\*(.+?)\\*"), "$1")
        blocks.add(ContentBlock(BlockType.TEXT, cleanText))
    }

    return blocks
}
