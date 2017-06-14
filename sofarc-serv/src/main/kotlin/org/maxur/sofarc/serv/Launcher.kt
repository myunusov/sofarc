package org.maxur.sofarc.serv

import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.hk2.IoC
import org.slf4j.LoggerFactory

/**
 * Application Launcher
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
object Launcher {

    /**
     * Command line entry point. This method kicks off the building of a application  object
     * and executes it.

     * @param args - arguments of command.
     */
    @JvmStatic fun main(args: Array<String>) {
        val application: MicroService? = IoC.application()
        application?.start() ?:
                log().error("Application is not configured")
    }

    private fun log() = LoggerFactory.getLogger(Launcher::class.java)
    
}

