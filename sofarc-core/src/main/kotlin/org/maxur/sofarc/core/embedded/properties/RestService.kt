package org.maxur.sofarc.core.embedded.properties

import com.fasterxml.jackson.annotation.JsonProperty

data class RestService(
        @JsonProperty("name", required = false) val name: String?,
        @JsonProperty("path") val path: String
)

