@file:Suppress("unused")

package org.maxur.sofarc.core.embedded.grizzly

import io.swagger.jaxrs.config.BeanConfig
import io.swagger.jaxrs.listing.ApiListingResource
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.*
import org.maxur.sofarc.core.embedded.properties.StaticContent
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
        // TODO mov this logic to domain
        val webAppProperties: WebAppProperties = properties.get(locator)!!
        val restServiceName = webAppProperties.rest!!.name
        val restConfig : RestResourceConfig = locator.service(RestResourceConfig::class.java, restServiceName) ?:
                return resourceConfigNotFoundError(locator, restServiceName ?: "undefined")

        val staticContent = ArrayList<StaticContent>()

        val config = RestAppConfig(
                webAppProperties.url,
                webAppProperties.rest,
                staticContent,
                restConfig
        )
        staticContent.addAll(webAppProperties.staticContent)
        if (webAppProperties.withHalBrowser) {
            staticContent.add(halContent())
        }
        if (webAppProperties. withSwaggerUi) {
            val doc = swaggerContent()
            staticContent.add(doc)
            initSwagger(restConfig.packages, config)
            restConfig.resources(ApiListingResource::class.java.`package`.name)
        }

        return WebServerGrizzlyImpl(config, locator)
    }

    private fun swaggerContent(): StaticContent {
        return StaticContent(
                arrayOf(URI("classpath:/META-INF/resources/webjars/swagger-ui/3.0.14/")),
                "docs",
                "index.html",
                "/index.html?url=/api/swagger.json"
        )
    }

    private fun halContent(): StaticContent {
        return StaticContent(
                arrayOf(URI("classpath:/META-INF/resources/webjars/hal-browser/3325375/")),
                "hal",
                "browser.html",
                "/#/api/service"
        )
    }

    private fun initSwagger(packages: MutableList<String>, restConfig: RestAppConfig) {
        val config = BeanConfig()
        config.basePath = "/" + restConfig.rest.path
        config.host = "${restConfig.url.host}:${restConfig.url.port}"
        config.resourcePackage = packages.joinToString(",")
        config.scan = true
    }

    private fun resourceConfigNotFoundError(locator: Locator, name: String): EmbeddedService? {
        val list = locator.names(ResourceConfig::class.java)
        throw IllegalStateException(
            "Resource Config '$name' is not supported. Try one from this list: $list or create one"
        )
    }

}

open class WebServerGrizzlyImpl(
        private val config: RestAppConfig,
        private val locator: Locator
) : WebServer() {

    fun ServerConfiguration.title(): String = "$name '$httpServerName-$httpServerVersion'"

    private lateinit var httpServer: HttpServer

    override val baseUri: URI get() = config.url

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
                URI("${config.url}/${config.rest.path}"),
                config.resourceConfig,
                locator.implementation<ServiceLocator>()
        )
        server.serverConfiguration.isPassTraceRequest = true
        server.serverConfiguration.defaultQueryEncoding = Charsets.UTF_8
        return server
    }

    private fun makeStaticHandlers(serverConfiguration: ServerConfiguration) {
        config.staticContent.forEach {
            serverConfiguration.addHttpHandler(
                    CompositeStaticHttpHandler.make(it),
                    "/${it.normalisePath}"
            )
        }
    }
    
    override fun entries(): WebEntries {
        val cfg = httpServer.serverConfiguration
        val entries = WebEntries(config.url)
        cfg.httpHandlersWithMapping.forEach {
            (_, regs) -> run {
                for (reg in regs) entries.add(
                        reg.contextPath,
                        reg.urlPattern,
                        config.staticContentByPath(reg.contextPath)?.startUrl ?: ""
                )
            }
        }
        return entries
    }

}