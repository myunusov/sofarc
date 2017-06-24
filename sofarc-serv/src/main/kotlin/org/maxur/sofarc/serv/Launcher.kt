package org.maxur.sofarc.serv

import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.MicroService.Companion.service
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
                .name(":name")
                .config().fromClasspath().rootKey("DEFAULTS")
                .embed("Grizzly")  // .asWebService() // .confuguredBy(":webapp")
                .beforeStart(this::beforeStart)
                .afterStop(this::afterStop)
                .onError(this::onError)
                .start()
    }
    
    fun beforeStart(service: MicroService) {
        (service.bean(ConfigParams::class.java))!!.log()
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

