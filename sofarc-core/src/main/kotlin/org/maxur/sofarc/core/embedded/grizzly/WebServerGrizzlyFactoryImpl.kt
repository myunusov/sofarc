@file:Suppress("unused")

package org.maxur.sofarc.core.embedded.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.embedded.*
import org.maxur.sofarc.core.embedded.properties.WebAppProperties
import org.maxur.sofarc.core.rest.RestResourceConfig
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

    override fun make(cfg: ServiceConfig): EmbeddedService? =
            WebServerGrizzlyImpl(properties(cfg)!!, config, locator)

}

open class WebServerGrizzlyImpl(
        private val properties: WebAppProperties,
        private val config: RestResourceConfig,
        private val locator: Locator) : WebServer() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    fun ServerConfiguration.title(): String = "$name '$httpServerName-$httpServerVersion'"

    private lateinit var httpServer: HttpServer

    override val baseUri: URI get() = properties.url

    override val name: String get() = httpServer.serverConfiguration.title()

    override fun entries(): WebEntries {
        val cfg = httpServer.serverConfiguration
        val entries = WebEntries(properties.url)
        cfg.httpHandlersWithMapping.forEach {
            (_, regs) -> run {
                for (reg in regs) entries.add(
                        reg.contextPath,
                        reg.urlPattern,
                        properties.findStaticContentByPath(reg.contextPath)?.startUrl ?: ""
                )
            }
        }
        return entries
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
                    CompositeStaticHttpHandler.make(it),
                    "/${it.normalisePath}"
            )
        }
    }


    private fun makeLoggerBridge() {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }

}