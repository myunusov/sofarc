package org.maxur.sofarc.core.embedded

import org.glassfish.jersey.server.ResourceConfig
import org.maxur.sofarc.core.embedded.properties.RestService
import org.maxur.sofarc.core.embedded.properties.StaticContent
import java.net.URI

data class RestAppConfig (
        val url: URI,
        val rest: RestService,
        val staticContent: List<StaticContent>,
        val resourceConfig: ResourceConfig
) {

    // TODO condition must be rewrite
    fun staticContentByPath(path: String): StaticContent? =
            staticContent.filter { it.path == path || "/${it.path}" == path }.firstOrNull()
    
}

