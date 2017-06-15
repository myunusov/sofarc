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
 * @param params                service's config params
 * @param webServer             web Server
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class SofArcService @Inject constructor(
        @Value(key = "name") name: String,
        @Named("Grizzly") val webServer: WebServer,
        val params: ConfigParams
) : MicroService(name, webServer) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(SofArcService::class.java)
    }

    override fun beforeStart() {
        params.log()
        log.info("$name is started")
    }

    override fun onError(exception: Exception) {
        log.error(exception.message, exception)
        stop()
    }

    override fun afterStop() {
        log.info("$name is stopped")
    }

}

