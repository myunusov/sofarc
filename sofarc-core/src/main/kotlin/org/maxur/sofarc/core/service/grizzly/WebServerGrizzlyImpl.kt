@file:Suppress("unused")

package org.maxur.sofarc.core.service.grizzly

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.grizzly.http.server.ServerConfiguration
import org.glassfish.grizzly.servlet.WebappContext
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.core.service.grizzly.config.StaticContent
import org.maxur.sofarc.core.service.grizzly.config.WebAppConfig
import org.maxur.sofarc.core.service.hk2.DSL.cfg
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.*
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
) : WebServer(webConfig.url) {

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
                for (reg in regs) {
                    val basePath = "${webConfig.url}${reg.contextPath}"
                    when {
                        reg.contextPath == "/docs" && webConfig.withSwaggerUi ->
                            log.info("$basePath/index.html?url=/api/swagger.json")
                        reg.contextPath == "/hal" && webConfig.withHalBrowser ->
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
        val context = WebappContext("grizzly web context", "")

        /*
        <context-param>
        <param-name>shiroConfigLocations</param-name>
        <param-value>classpath:shiro.ini</param-value>
        </context-param>
        */
        context.addContextInitParameter("shiroConfigLocations", "classpath:shiro.ini")

/*
        <listener>
        <listener-class>org.apache.shiro.web.env.EnvironmentLoaderListener</listener-class>
        </listener>
*/
        context.addListener(org.apache.shiro.web.env.EnvironmentLoaderListener::class.java)

/*
        <filter>
        <filter-name>ShiroFilter</filter-name>
        <filter-class>org.apache.shiro.web.servlet.ShiroFilter</filter-class>
        </filter>
*/
        val f1 = context.addFilter("f1", BasicHttpAuthenticationFilter::class.java)
        val shiroFilter = context.addFilter("ShiroFilter", org.apache.shiro.web.servlet.ShiroFilter::class.java)

/*        <filter-mapping>
            <filter-name>ShiroFilter</filter-name>
            <url-pattern>*//*</url-pattern>
            <dispatcher>REQUEST</dispatcher>
            <dispatcher>FORWARD</dispatcher>
            <dispatcher>INCLUDE</dispatcher>
            <dispatcher>ERROR</dispatcher>
            </filter-mapping>
*/
        shiroFilter.addMappingForUrlPatterns(EnumSet.allOf(javax.servlet.DispatcherType::class.java), "/api/**")
        f1.addMappingForUrlPatterns(EnumSet.allOf(javax.servlet.DispatcherType::class.java), "/**")


        val server = GrizzlyHttpServerFactory.createHttpServer(webConfig.apiUri, config, locator)

        context.deploy(server)
        server.serverConfiguration.isPassTraceRequest = true
        server.serverConfiguration.defaultQueryEncoding = Charsets.UTF_8
        return server
    }

    private fun makeStaticHandlers(serverConfiguration: ServerConfiguration) {
        webConfig.staticContent.forEach {
            serverConfiguration.addHttpHandler(
                    StaticHttpHandler(it),
                    "/${it.normalisePath}"
            )
        }

        if (webConfig.withHalBrowser) {
            addSwaggerUi(serverConfiguration)
        }

        if (webConfig.withSwaggerUi) {
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
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.getClassLoader(), doc),
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
                CLStaticHttpHandler(WebServerGrizzlyImpl::class.java.getClassLoader(), hal),
                hal.normalisePath
        )
    }

    private fun makeLoggerBridge() {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }


    override fun clone(key: String?): WebServerGrizzlyImpl {
        if (key == null) return this
        return WebServerGrizzlyImpl(cfg(key).asClass(WebAppConfig::class.java).get(), config, locator)
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