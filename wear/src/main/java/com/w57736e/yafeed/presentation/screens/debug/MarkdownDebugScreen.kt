package com.w57736e.yafeed.presentation.screens.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import com.w57736e.yafeed.markdown.WearMarkdown
import com.w57736e.yafeed.markdown.markdownComponents
import com.w57736e.yafeed.markdown.wearMarkdownColor
import com.w57736e.yafeed.markdown.wearMarkdownTypography

@Composable
fun MarkdownDebugScreen() {
    val scrollState = rememberScalingLazyListState()

    ScreenScaffold(scrollState = scrollState) {
        WearMarkdown(
            content = MARKDOWN_TEST_CONTENT,
            modifier = Modifier.fillMaxSize(),
            state = scrollState
        )
    }
}

private const val MARKDOWN_TEST_CONTENT = """
# Heading 1
## Heading 2
### Heading 3

This is **bold text** and this is *italic text*.

Here's a [link](https://example.com) and `inline code`.

## Lists

### Unordered List
- Item 1
- Item 2
  - Nested item
- Item 3

### Ordered List
1. First item
2. Second item
3. Third item

## Code Block

```kotlin
fun main() {
    println("Hello, Markdown!")
}
```

## Blockquote

> This is a blockquote.
> It can span multiple lines.

## Task List

- [x] Completed task
- [ ] Incomplete task
- [ ] Another task

## Table

| Header 1 | Header 2 |
|----------|----------|
| Cell 1   | Cell 2   |
| Cell 3   | Cell 4   |

## Horizontal Rule

---

## Mixed HTML

<p>This is <strong>HTML</strong> mixed with markdown.</p>

<img src="https://via.placeholder.com/150" alt="Test Image">

## End of Test
"""
