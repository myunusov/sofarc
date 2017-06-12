package org.maxur.sofarc.params

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

/**
 * The Web Application parameters.
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
data class WebApp(
        @JsonProperty("url") val url: URL,
        @JsonProperty("folderName") val folderName: String
)