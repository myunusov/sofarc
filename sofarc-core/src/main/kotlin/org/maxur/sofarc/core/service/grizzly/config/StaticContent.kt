@file:Suppress("unused")

package org.maxur.sofarc.core.service.grizzly.config

import com.fasterxml.jackson.annotation.JsonProperty

class StaticContent(
        @JsonProperty("roots")         val roots: Array<String>,
        @JsonProperty("path")          val path: String,
        @JsonProperty("default-page", required = false) page: String?
) {
    val page = page ?: "index.html"
}