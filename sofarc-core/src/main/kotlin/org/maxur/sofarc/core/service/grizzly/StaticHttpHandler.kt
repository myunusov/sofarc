package org.maxur.sofarc.core.service.grizzly

import org.glassfish.grizzly.Grizzly
import org.glassfish.grizzly.http.Method
import org.glassfish.grizzly.http.server.HttpHandler
import org.glassfish.grizzly.http.server.Request
import org.glassfish.grizzly.http.server.Response
import org.glassfish.grizzly.http.server.StaticHttpHandlerBase
import org.glassfish.grizzly.http.util.Header
import org.glassfish.grizzly.http.util.HttpStatus
import org.glassfish.grizzly.utils.ArraySet
import org.maxur.sofarc.core.service.grizzly.config.StaticContent
import java.io.File
import java.util.logging.Level

/**
 * [HttpHandler], which processes requests to a static resources.

 * @author Jeanfrancois Arcand
 *
 * @author Alexey Stashok
 *
 * @author Maxim Yunusov
 */
class StaticHttpHandler(defaultPage: String = "index.html") : StaticHttpHandlerBase() {

    /**
     * docRoots the list of directories where files will be serviced from.
     */
    private val docRoots = ArraySet(File::class.java)

    /**
     * Return <tt>true</tt> if HTTP 301 redirect shouldn't be sent when requested
     *       static resource is a directory, or <tt>false</tt> otherwise
     */
    private val isDirectorySlashOff: Boolean = false


    /**
     *  defaultPage is the default page name of all roots.
     */
    private val defaultPage = defaultPage


    /**
     * Create a new instance which will look for static pages located
     * under the <tt>docRoot</tt>. If the <tt>docRoot</tt> is <tt>null</tt> -
     * static pages won't be served by this <tt>HttpHandler</tt>

     * @param staticContent the static content configuration
     *   with the folder(s) where the static resource are located
     *   and default index page.
     * * If the <tt>docRoot</tt> is <tt>null</tt> - static pages won't be served
     * * by this <tt>HttpHandler</tt>
     */
    constructor(staticContent: StaticContent): this(staticContent.page)  {
        for (docRoot in staticContent.roots) {
            docRoots.add(File(docRoot))
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(Exception::class)
    override fun handle(uri: String,
                        request: Request,
                        response: Response): Boolean {

        var found = false

        val fileFolders = docRoots.array ?: return false

        var resource: File? = null

        for (i in fileFolders.indices) {
            val webDir = fileFolders[i]
            // local file
            resource = File(webDir, uri)
            val exists = resource.exists()
            val isDirectory = resource.isDirectory

            if (exists && isDirectory) {

                if (!isDirectorySlashOff && !uri.endsWith("/")) { // redirect to the same url, but with trailing slash
                    response.setStatus(HttpStatus.MOVED_PERMANENTLY_301)
                    response.setHeader(Header.Location,
                            response.encodeRedirectURL(uri + "/"))
                    return true
                }

                val f = File(resource, "/$defaultPage")
                if (f.exists()) {
                    resource = f
                    found = true
                    break
                }
            }

            if (isDirectory || !exists) {
                found = false
            } else {
                found = true
                break
            }
        }

        if (!found) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "File not found {0}", resource)
            }
            return false
        }

        assert(resource != null)

        // If it's not HTTP GET - return method is not supported status
        if (Method.GET != request.method) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "File found {0}, but HTTP method {1} is not allowed",
                        arrayOf<Any>(resource!!, request.method))
            }
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405)
            response.setHeader(Header.Allow, "GET")
            return true
        }

        StaticHttpHandlerBase.pickupContentType(response, resource!!.path)

        addToFileCache(request, response, resource)
        StaticHttpHandlerBase.sendFile(response, resource)

        return true
    }

    companion object {
        private val LOGGER = Grizzly.logger(StaticHttpHandler::class.java)
    }




}


