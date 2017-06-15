package org.maxur.sofarc.core.service.grizzly

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI

/**
 * The Web Application parameters.
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
data class WebAppConfig(
        @JsonProperty("url") val url: URI,
        @JsonProperty("folder-name") val folderName: String,
        @JsonProperty("api-path") val apiPath: String
) {
    val apiUri: URI = URI.create("$url/$apiPath")
}