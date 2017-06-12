@file:Suppress("unused")

package org.maxur.sofarc.serv

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.params.ConfigParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Named

/**
 *
 * The SofArcService type.
 *
 * @param webServer             web Server
 *
 * @param params                service's config params * @param webServer             web Server
 *
 * @param params                service's config params
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class SofArcService @Inject constructor(
        @Named("Grizzly") val webServer: WebServer,
        val params: ConfigParams
) : MicroService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(SofArcService::class.java)
    }

    @Value("name")
    override lateinit var name: String

    override fun start() {
        params.log()
        webServer.start()
        log.info("$name is started")
    }

    override fun stop() {
        webServer.stop()
        log.info("$name is stopped")
    }

}

