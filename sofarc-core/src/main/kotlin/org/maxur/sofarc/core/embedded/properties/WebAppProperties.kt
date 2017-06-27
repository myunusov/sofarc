package org.maxur.sofarc.core.embedded.properties

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
        @JsonProperty("rest", required = false) rest: Array<RestService>?,
        @JsonProperty("static-content", required = false) staticContent: Array<StaticContent>?,
        @JsonProperty("with-hal-browser", required = false) val withHalBrowser: Boolean = false,
        @JsonProperty("with-swagger-ui", required = false) val withSwaggerUi: Boolean = false
) {
    val staticContent: MutableList<StaticContent> =
            if (staticContent != null)
                mutableListOf(*staticContent)
            else
                ArrayList()

    private val rests: MutableList<RestService> =
            if (rest != null)
                mutableListOf(*rest)
            else
                ArrayList()

    val rest: RestService = when(this.rests.size) {
        0 -> throw IllegalStateException("Current version needs in rest service")
        1 -> this.rests[0]
        else -> throw IllegalStateException("Current version support only one rest service")
    }

    init {
        if (withHalBrowser) {
            val hal = StaticContent(
                    arrayOf(URI("classpath:/META-INF/resources/webjars/hal-browser/3325375/")),
                    "hal",
                    "browser.html",
                    "/#/api/service"
            )
            this.staticContent.add(hal)
        }
        if (withSwaggerUi) {
            val doc = StaticContent(
                    arrayOf(URI("classpath:/META-INF/resources/webjars/swagger-ui/3.0.14/")),
                    "docs",
                    "index.html",
                    "/index.html?url=/api/swagger.json"
            )
            this.staticContent.add(doc)
        }

    }

    fun findStaticContentByPath(path: String): StaticContent? {
        // TODO condition must be rewrite
        val list = staticContent.filter { it.path == path || "/${it.path}" == path }
        if (list.isEmpty()) {
            return null
        } else {
            return list.first()
        }

    }

    override fun toString(): String {
        val mapper = ObjectMapperProvider().provide()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(this)
    }




}

