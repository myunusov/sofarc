@file:Suppress("unused")

package org.maxur.sofarc.core.service.eservice.grizzly.properties

import com.fasterxml.jackson.annotation.JsonProperty

class StaticContent(
        @JsonProperty("roots")         val roots: Array<String>,
        @JsonProperty("path")          val path: String,
        @JsonProperty("default-page", required = false) page: String?
) {
    val page = page ?: "index.html"

    val normalisePath: String = normalisePath(path)
    
    private fun normalisePath(path: String): String {
        val ex = path.replace("/{2,}".toRegex(), "/")
        return if (ex.endsWith("/")) ex.substring(0, ex.length - 1) else ex
    }
    
}