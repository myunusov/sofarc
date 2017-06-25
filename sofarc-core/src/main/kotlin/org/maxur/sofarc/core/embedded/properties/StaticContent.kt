@file:Suppress("unused")

package org.maxur.sofarc.core.embedded.properties

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

class StaticContent(
        @JsonProperty("roots")         val roots: Array<URI>,
        @JsonProperty("path")          val path: String,
        @JsonProperty("default-page", required = false) page: String?
) {
    val page = page ?: "index.html"

    val normalisePath: String = normalisePath(path)

    private val schemes: Array<String> = roots.map { it.scheme }.distinct().toTypedArray()

    val scheme: String = when {
        schemes contentEquals arrayOf(null, "file") -> "file"
        schemes contentEquals arrayOf("classpath") -> "classpath"
        else -> "unsupported"
    }
    
    private fun normalisePath(path: String): String {
        val ex = path.replace("/{2,}".toRegex(), "/")
        return if (ex.endsWith("/")) ex.substring(0, ex.length - 1) else ex
    }
    
}