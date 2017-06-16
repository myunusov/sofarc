package org.maxur.sofarc.core.rest

import com.google.common.io.Resources
import org.maxur.sofarc.core.annotation.Value
import java.io.IOException
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo


/**
 * The type Documentation resource.
 *
 * @author Maxim Yunusov
 * @version 1.0
 * @since <pre>11/29/13</pre>
 */
@Path("/swagger.json")
open class DocResource(@Value(key = "webapp.api-path") val path: String) {

    /**
     * Gets a application documentation
     *
     * @param uriInfo uriInfo
     * @return response ok
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun swaggerDoc(@Context uriInfo: UriInfo): String {
        val uri = uriInfo.baseUri
        val baseUri = "$uri"
        val url = Resources.getResource("swagger.json")
        try {
            val swaggerDoc: String = Resources.toString(url, Charsets.UTF_8)
            return swaggerDoc.replace("baseurl", baseUri)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

}

