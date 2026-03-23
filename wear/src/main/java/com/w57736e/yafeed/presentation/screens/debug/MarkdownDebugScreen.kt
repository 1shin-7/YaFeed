package com.w57736e.yafeed.presentation.screens.debug

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import com.w57736e.yafeed.markdown.WearMarkdown

@Composable
fun MarkdownDebugScreen() {
    val scrollState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = scrollState) {
        WearMarkdown(
            content = MARKDOWN_TEST_CONTENT,
            modifier = Modifier.fillMaxSize(),
            state = scrollState
        )
    }
}

private const val MARKDOWN_TEST_CONTENT = """
# ATX Heading 1
## ATX Heading 2
### ATX Heading 3
#### ATX Heading 4
##### ATX Heading 5
###### ATX Heading 6

Setext Heading 1
================

Setext Heading 2
----------------

**Bold** *Italic* ***Bold Italic*** ~~Strikethrough~~

Paragraph with `inline code` and autolink: https://example.com

[Link text](https://example.com "Title")
[Reference link][ref]

[ref]: https://example.com

![Image](https://placehold.co/600x400/png "Image Title")

---

> Blockquote
> Multiple lines
>> Nested blockquote

- Unordered list
- Item 2
  - Nested item
    - Deep nested

1. Ordered list
2. Item 2
   1. Nested ordered
   2. Item 2

- [x] Task 1 done
- [ ] Task 2 todo
  - [x] Nested task done
  - [ ] Nested task todo

```kotlin
// Fenced code block
fun main() {
    println("Hello")
}
```

    Indented code block
    Line 2

| Header 1 | Header 2 | Header 3 |
|----------|:--------:|---------:|
| Left     | Center   | Right    |
| Cell     | Cell     | Cell     |

<div class="html-block">
  <h1>HTML Heading</h1>
  <h2>Subheading</h2>
  <p>Paragraph with <strong>bold</strong>, <em>italic</em>, <code>code</code></p>
  <p><a href="https://example.com">Link</a></p>
  <img src="https://placehold.co/300x200/png" alt="HTML Image">
  <ul>
    <li>HTML list 1</li>
    <li>HTML list 2
      <ul>
        <li>Nested HTML list</li>
      </ul>
    </li>
  </ul>
  <ol>
    <li>Ordered 1</li>
    <li>Ordered 2</li>
  </ol>
  <blockquote>HTML blockquote</blockquote>
  <pre><code>HTML code block</code></pre>
  <table>
    <tr><th>HTML Table</th><th>Header</th></tr>
    <tr><td>Cell 1</td><td>Cell 2</td></tr>
  </table>
  <hr>
  <span>Span element</span>
  <div>Nested div</div>
</div>

Inline HTML: <strong>bold</strong> <em>italic</em> <code>code</code>
"""