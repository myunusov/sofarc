@file:Suppress("unused")

package org.maxur.sofarc.core.embedded.grizzly

import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.embedded.WebEntries
import org.maxur.sofarc.core.embedded.WebServer
import org.maxur.sofarc.core.embedded.properties.WebAppProperties
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.slf4j.bridge.SLF4JBridgeHandler
import java.net.URI
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
@Service(name = "Grizzly")
class WebServerGrizzlyFactoryImpl @Inject constructor(val locator: Locator) : EmbeddedServiceFactory() {

    companion object {
        init {
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()
        }
    }

    override fun make(properties: Holder<Any?>): EmbeddedService? {
        val webAppProperties: WebAppProperties = properties.get(locator)!!
        val restServiceName = webAppProperties.rest.name
        val config : RestResourceConfig = locator.service(RestResourceConfig::class.java, restServiceName) ?:
                return resourceConfigNotFoundError(locator, restServiceName ?: "undefined")
        return WebServerGrizzlyImpl(webAppProperties, config, locator)
    }


    private fun resourceConfigNotFoundError(locator: Locator, name: String): EmbeddedService? {
        val list = locator.names(ResourceConfig::class.java)
        throw IllegalStateException(
            "Resource Config '$name' is not supported. Try one from this list: $list or create one"
        )
    }

}

open class WebServerGrizzlyImpl(
        private val properties: WebAppProperties,
        private val config: ResourceConfig,
        private val locator: Locator
) : WebServer() {

    fun ServerConfiguration.title(): String = "$name '$httpServerName-$httpServerVersion'"

    private lateinit var httpServer: HttpServer

    override val baseUri: URI get() = properties.url

    override val name: String get() = httpServer.serverConfiguration.title()

    override fun launch() {
        val result = httpServer()
        makeStaticHandlers(result.serverConfiguration)
        httpServer = result
    }

    override fun shutdown() {
        httpServer.shutdownNow()
    }

    private fun httpServer(): HttpServer {
        val server = GrizzlyHttpServerFactory.createHttpServer(
                URI("${properties.url}/${properties.rest.path}"),
                config,
                locator.implementation<ServiceLocator>()
        )
        server.serverConfiguration.isPassTraceRequest = true
        server.serverConfiguration.defaultQueryEncoding = Charsets.UTF_8
        return server
    }

    private fun makeStaticHandlers(serverConfiguration: ServerConfiguration) {
        properties.staticContent.forEach {
            serverConfiguration.addHttpHandler(
                    CompositeStaticHttpHandler.make(it),
                    "/${it.normalisePath}"
            )
        }
    }
    
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

}