package org.maxur.sofarc.serv

import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.hk2.DSL.cfg
import org.maxur.sofarc.core.service.hk2.DSL.service
import org.maxur.sofarc.core.service.hk2.DSL.webService
import org.maxur.sofarc.params.ConfigParams
import org.slf4j.LoggerFactory

/**
 * Application Launcher
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
object Launcher {

    private fun log() = LoggerFactory.getLogger(Launcher::class.java)

    /**
     * Command line entry point. This method kicks off the building of a application  object
     * and executes it.
     *
     * @param args - arguments of command.
     */
    @JvmStatic fun main(args: Array<String>)  {
        service()
                .name(cfg("name"))
                .config(ConfigSource.fromClasspath().rootKey("DEFAULTS").writeTo(ConfigParams::class.java))
                .embed(webService("Grizzly"))
                .beforeStart( { ms ->  beforeStart(ms) })
                .afterStop( { ms ->  afterStop(ms) })
                .onError( { ms, e ->  onError(ms, e) })
                .start()
    }
    
    fun beforeStart(service: MicroService) {
        (service.config as ConfigParams).log()
        log().info("${service.name} is started")
    }


    fun afterStop(service: MicroService) {
        log().info("${service.name} is stopped")
    }

    fun onError(service: MicroService, exception: Exception) {
        log().error(exception.message, exception)
        service.stop()
    }

}

