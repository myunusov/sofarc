@file:Suppress("unused")

package org.maxur.sofarc.core.service.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.core.service.grizzly.config.StaticContent
import org.maxur.sofarc.core.service.grizzly.config.WebAppConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import javax.inject.Inject
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext


/**
 * The Grizzly Embedded Web Server Adapter
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
@Inject constructor(
        @Value(key = "webapp") val webConfig: WebAppConfig,
        val config: RestResourceConfig,
        val locator: ServiceLocator
) : WebServer(webConfig.url, webConfig.apiPath) {


    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    private lateinit var httpServer: HttpServer

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
                regs.filter { it.contextPath != "/docs" }.forEach {
                    log.info("${webConfig.url}${it.contextPath}/")
                }
                regs.filter { it.contextPath == "/docs" }.forEach {
                    log.info("${webConfig.url}${it.contextPath}/index.html?url=/api/swagger.json")
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
        val server = GrizzlyHttpServerFactory.createHttpServer(webConfig.apiUri, config, locator)
        val result = server
        result.serverConfiguration.isPassTraceRequest = true
        result.serverConfiguration.defaultQueryEncoding = Charsets.UTF_8
        return result
    }

    private fun makeStaticHandlers(serverConfiguration: ServerConfiguration) {
        webConfig.staticContent.forEach {
            serverConfiguration.addHttpHandler(
                    StaticHttpHandler(it),
                    "/${it.normalisePath}"
            )
        }


        // TODO default browser.html
        val hal = StaticContent(
                arrayOf("/META-INF/resources/webjars/hal-browser/3325375/"),
                "/hal",
                "browser.html"
        )
        serverConfiguration.addHttpHandler(
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.getClassLoader(), hal),
                hal.normalisePath
        )
        
        // TODO default index.html?url=%2Fapi/swagger.json
        val doc = StaticContent(
                arrayOf("/META-INF/resources/webjars/swagger-ui/3.0.14/"),
                "/docs",
                "index.html"
        )

        serverConfiguration.addHttpHandler(
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.getClassLoader(), doc),
                doc.normalisePath
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