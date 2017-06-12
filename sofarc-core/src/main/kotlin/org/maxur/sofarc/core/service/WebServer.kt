package org.maxur.sofarc.core.service

import org.jvnet.hk2.annotations.Contract
import org.maxur.sofarc.core.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
abstract class WebServer {

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServer::class.java)
    }

    /**
     * webapp folder url
     */
    protected val WEB_APP_URL = "/"


    /**
     * WebApp URL
     */
    @SuppressWarnings("unused")
    @Value("webapp.url")
    lateinit var webappUri: URI

    /**
     * Start Web server.
     */
    fun start() {
        log.info("Start Web Server")
        launch()
        log.info("Starting on " + webappUri)
    }

    /**
     * Stop Web server.
     */
    fun stop() {
        log.info("Stop Web Server")
        shutdown()
    }


    /**
     * Gets webapp uri.
     *
     * @return the webapp uri
     */
    protected fun webappUri(): URI {
        return webappUri
    }

    /**
     * web server launch
     */
    protected abstract fun launch()

    /**
     * web server shutdown
     */
    protected abstract fun shutdown()


}
