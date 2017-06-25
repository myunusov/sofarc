@file:Suppress("unused")

package org.maxur.sofarc.core.service.embedded.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.embedded.EmbeddedService
import org.maxur.sofarc.core.service.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.embedded.ServiceDescriptor
import org.maxur.sofarc.core.service.embedded.WebServer
import org.maxur.sofarc.core.service.embedded.properties.StaticContent
import org.maxur.sofarc.core.service.embedded.properties.WebAppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.net.URI
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
@Service(name = "Grizzly")
class WebServerGrizzlyFactoryImpl @Inject constructor(val config: RestResourceConfig)
    : EmbeddedServiceFactory<WebAppProperties>() {

    override fun make(cfg: ServiceDescriptor<WebAppProperties>): EmbeddedService? =
            WebServerGrizzlyImpl(properties(cfg)!! , config, locator)

}

open class WebServerGrizzlyImpl(
        private val properties: WebAppProperties,
        private val config: RestResourceConfig,
        private val locator: Locator) : WebServer() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    private lateinit var httpServer: HttpServer

    override val baseUri: URI get() = properties.url

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
        val implementation: ServiceLocator = locator.implementation<ServiceLocator>()
        val server = GrizzlyHttpServerFactory.createHttpServer(properties.apiUri, config, implementation)
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

}