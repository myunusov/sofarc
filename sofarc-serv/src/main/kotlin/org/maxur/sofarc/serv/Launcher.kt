package org.maxur.sofarc.serv

import org.maxur.sofarc.core.service.IoC
import org.maxur.sofarc.core.service.MicroService
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
        val locator = IoC.newLocator()
        val application = locator.getService<MicroService>(MicroService::class.java)
        application?.start() ?:
                LoggerFactory.getLogger(Launcher::class.java).error("Application is not configured")
    }


}

