@file:Suppress("unused")

package org.maxur.sofarc.core.service.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.StaticHttpHandler
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.WebServer
import javax.inject.Inject

/**
 * The Embeded Grizzly Embedded Web Server Adapter
 *
 * @param config  resource configuration
 * *
 * @param locator service locator (hk2)
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service(name = "Grizzly")
open class WebServerGrizzlyImpl
    @Inject constructor(val config: RestResourceConfig, val locator: ServiceLocator): WebServer() {

    private lateinit var httpServer: HttpServer

    @Value("webapp.folderName")
    private lateinit var webappFolderName: String

    override fun launch() {
        httpServer = GrizzlyHttpServerFactory.createHttpServer(
                webappUri(),
                config,
                locator
        )
        httpServer.serverConfiguration.addHttpHandler(
                StaticHttpHandler(webappFolderName),
                WEB_APP_URL + "api-docs"
        )
    }

    override fun shutdown() {
        httpServer.shutdownNow()
    }

}