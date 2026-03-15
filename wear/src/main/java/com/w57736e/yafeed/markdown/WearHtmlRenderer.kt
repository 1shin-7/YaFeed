package com.w57736e.yafeed.markdown

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.model.MarkdownTypography
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun WearHtmlRenderer(htmlContent: String, typography: MarkdownTypography) {
    val markdown = convertHtmlToMarkdown(htmlContent)
    val parser = MarkdownParser(GFMFlavourDescriptor())
    val tree = parser.buildMarkdownTreeFromString(markdown)
    val components = LocalMarkdownComponents.current

    Column {
        tree.children.forEach { child ->
            MarkdownElement(
                node = child,
                components = components,
                content = markdown,
                includeSpacer = false
            )
        }
    }
}

fun convertHtmlToMarkdown(html: String): String {
    var result = html

    // Headings
    result = result.replace(Regex("<h1>(.*?)</h1>", RegexOption.DOT_MATCHES_ALL)) { "# ${it.groupValues[1].trim()}\n" }
    result = result.replace(Regex("<h2>(.*?)</h2>", RegexOption.DOT_MATCHES_ALL)) { "## ${it.groupValues[1].trim()}\n" }
    result = result.replace(Regex("<h3>(.*?)</h3>", RegexOption.DOT_MATCHES_ALL)) { "### ${it.groupValues[1].trim()}\n" }
    result = result.replace(Regex("<h4>(.*?)</h4>", RegexOption.DOT_MATCHES_ALL)) { "#### ${it.groupValues[1].trim()}\n" }
    result = result.replace(Regex("<h5>(.*?)</h5>", RegexOption.DOT_MATCHES_ALL)) { "##### ${it.groupValues[1].trim()}\n" }
    result = result.replace(Regex("<h6>(.*?)</h6>", RegexOption.DOT_MATCHES_ALL)) { "###### ${it.groupValues[1].trim()}\n" }

    // Lists
    result = result.replace(Regex("<ul>(.*?)</ul>", RegexOption.DOT_MATCHES_ALL)) { match ->
        Regex("<li>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)
            .findAll(match.groupValues[1])
            .joinToString("\n") { "- ${it.groupValues[1].replace(Regex("<[^>]*>"), "").trim()}" } + "\n"
    }
    result = result.replace(Regex("<ol>(.*?)</ol>", RegexOption.DOT_MATCHES_ALL)) { match ->
        Regex("<li>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)
            .findAll(match.groupValues[1])
            .mapIndexed { index, it -> "${index + 1}. ${it.groupValues[1].replace(Regex("<[^>]*>"), "").trim()}" }
            .joinToString("\n") + "\n"
    }

    // Blockquote
    result = result.replace(Regex("<blockquote>(.*?)</blockquote>", RegexOption.DOT_MATCHES_ALL)) {
        "> ${it.groupValues[1].replace(Regex("<[^>]*>"), "").trim()}\n"
    }

    // Code blocks
    result = result.replace(Regex("<pre>(?:<code>)?(.*?)(?:</code>)?</pre>", RegexOption.DOT_MATCHES_ALL)) {
        "```\n${it.groupValues[1].trim()}\n```\n"
    }

    // Horizontal rule
    result = result.replace(Regex("<hr\\s*/?>")) { "---\n" }

    // Images
    result = result.replace(Regex("<img\\s+[^>]*src=[\"']([^\"']+)[\"'][^>]*(?:alt=[\"']([^\"']*)[\"'])?[^>]*>")) {
        "![${it.groupValues.getOrNull(2) ?: ""}](${it.groupValues[1]})\n"
    }

    // Inline elements
    result = result.replace(Regex("<strong>(.*?)</strong>")) { "**${it.groupValues[1]}**" }
    result = result.replace(Regex("<em>(.*?)</em>")) { "*${it.groupValues[1]}*" }
    result = result.replace(Regex("<code>(.*?)</code>")) { "`${it.groupValues[1]}`" }
    result = result.replace(Regex("<a\\s+href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>")) { "[${it.groupValues[2]}](${it.groupValues[1]})" }

    // Paragraphs
    result = result.replace(Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)) { "${it.groupValues[1]}\n\n" }

    // Remove remaining HTML tags
    result = result.replace(Regex("<[^>]*>"), "")

    return result.trim()
}

