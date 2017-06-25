package org.maxur.sofarc.core.service.embedded.properties

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.SerializationFeature
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import java.net.URI

/**
 * The Web Application parameters.
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
class WebAppProperties(
        @JsonProperty("url") val url: URI,
        @JsonProperty("api-path") val apiPath: String,
        @JsonProperty("static-content", required = false) staticContent: Array<StaticContent>?,
        @JsonProperty("with-hal-browser", required = false) val withHalBrowser: Boolean = false,
        @JsonProperty("with-swagger-ui", required = false) val withSwaggerUi: Boolean = false

) {
    val apiUri: URI = URI.create("$url/$apiPath")

    val staticContent = staticContent ?: emptyArray()

    override fun toString(): String {
        val mapper = ObjectMapperProvider().provide()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(this)
    }
}

