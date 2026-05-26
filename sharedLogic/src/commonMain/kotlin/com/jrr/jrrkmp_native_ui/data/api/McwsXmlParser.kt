package com.jrr.jrrkmp_native_ui.data.api

import kotlinx.serialization.Serializable

@Serializable
data class McwsResponse(
    val status: String,
    val items: Map<String, String>
)

/**
 * Parses standard MCWS XML response:
 * ```
 * <Response Status="OK">
 *   <Item Name="Key">Value</Item>
 * </Response>
 * ```
 */
fun parseMcwsResponse(xml: String): McwsResponse {
    var status = "Failure"
    val items = mutableMapOf<String, String>()

    val responseStart = xml.indexOf("<Response")
    if (responseStart != -1) {
        val responseEnd = xml.indexOf(">", responseStart)
        if (responseEnd != -1) {
            val responseTagContent = xml.substring(responseStart, responseEnd)
            status = extractAttribute(responseTagContent, "Status") ?: "Failure"
        }
    }

    var index = 0
    while (index < xml.length) {
        val itemStart = xml.indexOf("<Item", index)
        if (itemStart == -1) break

        val itemTagEnd = xml.indexOf(">", itemStart)
        if (itemTagEnd == -1) break

        val itemTagContent = xml.substring(itemStart, itemTagEnd)
        val name = extractAttribute(itemTagContent, "Name")

        val itemClose = xml.indexOf("</Item>", itemTagEnd)
        if (itemClose == -1) break

        if (name != null) {
            val rawValue = xml.substring(itemTagEnd + 1, itemClose)
            // Combine value chunks if needed, but standard items are flat single values
            val existing = items[name] ?: ""
            items[name] = existing + unescapeXml(rawValue)
        }

        index = itemClose + "</Item>".length
    }

    // Clean values by trimming
    val trimmedItems = items.mapValues { it.value.trim() }
    return McwsResponse(status = status, items = trimmedItems)
}

/**
 * Parses WebPlay lookup response:
 * ```
 * <Response>
 *   <ip>1.2.3.4</ip>
 *   <port>52199</port>
 * </Response>
 * ```
 */
fun parseMcwsWebPlayLookup(xml: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var index = 0
    while (index < xml.length) {
        val tagStart = xml.indexOf("<", index)
        if (tagStart == -1) break

        val tagEnd = xml.indexOf(">", tagStart)
        if (tagEnd == -1) break

        val tagContent = xml.substring(tagStart + 1, tagEnd).trim()
        // Ignore processing instructions, comments, or closing tags
        if (tagContent.startsWith("?") || tagContent.startsWith("!") || tagContent.startsWith("/")) {
            index = tagEnd + 1
            continue
        }

        val tagName = tagContent.split(" ")[0].trim()
        if (tagName == "Response") {
            index = tagEnd + 1
            continue
        }

        val closeTag = "</$tagName>"
        val closeTagIndex = xml.indexOf(closeTag, tagEnd)
        if (closeTagIndex == -1) {
            index = tagEnd + 1
            continue
        }

        val rawValue = xml.substring(tagEnd + 1, closeTagIndex)
        val existing = result[tagName] ?: ""
        result[tagName] = existing + unescapeXml(rawValue)
        index = closeTagIndex + closeTag.length
    }

    return result.mapValues { it.value.trim() }
}

private fun extractAttribute(tagContent: String, attributeName: String): String? {
    var searchIndex = 0
    while (searchIndex < tagContent.length) {
        val attrIndex = tagContent.indexOf(attributeName, searchIndex)
        if (attrIndex == -1) return null

        // Check if it's a real attribute name (must be preceded by whitespace or tag boundary)
        val charBefore = if (attrIndex > 0) tagContent[attrIndex - 1] else ' '
        val isWordBound = charBefore.isWhitespace()

        if (isWordBound) {
            val equalsIndex = tagContent.indexOf("=", attrIndex + attributeName.length)
            if (equalsIndex != -1) {
                val between = tagContent.substring(attrIndex + attributeName.length, equalsIndex)
                if (between.trim().isEmpty()) {
                    // Find opening quote
                    var quoteIndex = -1
                    var quoteChar = '"'
                    for (i in (equalsIndex + 1) until tagContent.length) {
                        val c = tagContent[i]
                        if (c == '"' || c == '\'') {
                            quoteIndex = i
                            quoteChar = c
                            break
                        }
                        if (!c.isWhitespace()) {
                            break
                        }
                    }
                    if (quoteIndex != -1) {
                        val closingQuoteIndex = tagContent.indexOf(quoteChar, quoteIndex + 1)
                        if (closingQuoteIndex != -1) {
                            return tagContent.substring(quoteIndex + 1, closingQuoteIndex)
                        }
                    }
                }
            }
        }
        searchIndex = attrIndex + 1
    }
    return null
}

private fun unescapeXml(input: String): String {
    if (!input.contains('&')) return input

    val sb = StringBuilder()
    var i = 0
    while (i < input.length) {
        val c = input[i]
        if (c == '&') {
            val end = input.indexOf(';', i)
            if (end != -1 && end - i < 10) {
                val entity = input.substring(i + 1, end)
                val unescaped = when (entity) {
                    "amp" -> "&"
                    "lt" -> "<"
                    "gt" -> ">"
                    "quot" -> "\""
                    "apos" -> "'"
                    else -> {
                        if (entity.startsWith("#")) {
                            try {
                                val code = if (entity.startsWith("#x")) {
                                    entity.substring(2).toInt(16)
                                } else {
                                    entity.substring(1).toInt(10)
                                }
                                code.toChar().toString()
                            } catch (e: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }
                }
                if (unescaped != null) {
                    sb.append(unescaped)
                    i = end + 1
                    continue
                }
            }
        }
        sb.append(c)
        i++
    }
    return sb.toString()
}
