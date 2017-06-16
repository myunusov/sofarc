package org.maxur.sofarc.core.service.grizzly.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.SerializationFeature
import org.maxur.sofarc.core.service.ObjectMapperProvider
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
        @JsonProperty("static-content") val staticContent: Array<StaticContent>,
        @JsonProperty("api-path") val apiPath: String
) {
    val apiUri: URI = URI.create("$url/$apiPath")

    override fun toString(): String {
        val mapper = ObjectMapperProvider().provide()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(this)
    }
}

