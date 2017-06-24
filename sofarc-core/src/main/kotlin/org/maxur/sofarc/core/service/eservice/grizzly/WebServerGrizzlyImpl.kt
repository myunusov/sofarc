@file:Suppress("unused")

package org.maxur.sofarc.core.service.eservice.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.Locator
import org.maxur.sofarc.core.service.eservice.WebServer
import org.maxur.sofarc.core.service.eservice.grizzly.properties.StaticContent
import org.maxur.sofarc.core.service.eservice.grizzly.properties.WebAppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.net.URI
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext


/**
 * The Grizzly Embedded Web Server Adapter
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
open class WebServerGrizzlyImpl : WebServer<WebAppProperties> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    private val config: RestResourceConfig

    private val locator: ServiceLocator

    private val properties: WebAppProperties

    private lateinit var httpServer: HttpServer

    override val baseUri: URI get() = properties.url

    constructor(
            properties: WebAppProperties,
            config: RestResourceConfig,
            locator: Locator
    ): super(properties) {
        this.config = config
        this.locator = locator.implementation()
        this.properties = properties()!!
    }

    constructor(
            propertyKey: String,
            config: RestResourceConfig,
            locator: Locator
    ): super(locator, propertyKey) {
        this.config = config
        this.locator = locator.implementation()
        this.properties = properties()!!
    }

    override val name: String
        get() {
            val cfg = httpServer.serverConfiguration
            return "${cfg.name} '${cfg.httpServerName}-${cfg.httpServerVersion}'"
        }

    override fun logEntries() {
        log.info("Entries:")
        val cfg = httpServer.serverConfiguration
        cfg.httpHandlersWithMapping.forEach {
            (_, regs) ->
            run {
                for (reg in regs) {
                    val basePath = "${properties.url}${reg.contextPath}"
                    when {
                        reg.contextPath == "/docs" && properties.withSwaggerUi ->
                            log.info("$basePath/index.html?url=/api/swagger.json")
                        reg.contextPath == "/hal" && properties.withHalBrowser ->
                            log.info("$basePath/#/api/service")
                        else -> log.info("$basePath/")
                    }
                }
                regs.filter { it.contextPath == "/docs" }.forEach {

                }
            }
        }
    }

    override fun launch() {
        makeLoggerBridge()
        val result = httpServer()
        makeStaticHandlers(result.serverConfiguration)
        httpServer = result
    }

    override fun shutdown() {
        httpServer.shutdownNow()
    }

    private fun httpServer(): HttpServer {
        val server = GrizzlyHttpServerFactory.createHttpServer(properties.apiUri, config, locator)
        val result = server
        result.serverConfiguration.isPassTraceRequest = true
        result.serverConfiguration.defaultQueryEncoding = Charsets.UTF_8
        return result
    }

    private fun makeStaticHandlers(serverConfiguration: ServerConfiguration) {
        properties.staticContent.forEach {
            serverConfiguration.addHttpHandler(
                    StaticHttpHandler(it),
                    "/${it.normalisePath}"
            )
        }

        if (properties.withHalBrowser) {
            addSwaggerUi(serverConfiguration)
        }

        if (properties.withSwaggerUi) {
            addHalBrowser(serverConfiguration)
        }
    }

    private fun addHalBrowser(serverConfiguration: ServerConfiguration) {
        val doc = StaticContent(
                arrayOf("/META-INF/resources/webjars/swagger-ui/3.0.14/"),
                "/docs",
                "index.html"
        )

        serverConfiguration.addHttpHandler(
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.classLoader, doc),
                doc.normalisePath
        )
    }

    private fun addSwaggerUi(serverConfiguration: ServerConfiguration) {
        val hal = StaticContent(
                arrayOf("/META-INF/resources/webjars/hal-browser/3325375/"),
                "/hal",
                "browser.html"
        )
        serverConfiguration.addHttpHandler(
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.classLoader, hal),
                hal.normalisePath
        )
    }

    private fun makeLoggerBridge() {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }
    /**
     * service locator feature
     */
    private class ServiceLocatorFeature : Feature {

        override fun configure(context: FeatureContext): Boolean {
            ServiceLocatorProvider.getServiceLocator(context)
            return true
        }
    }

}