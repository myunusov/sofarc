package org.maxur.sofarc.serv

import org.maxur.sofarc.core.BaseMicroService
import org.maxur.sofarc.core.service.hk2.DSL
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
        DSL.service {
            title = ":name"
            observers {
                beforeStart = this@Launcher::beforeStart
                afterStop = this@Launcher::afterStop
                onError = this@Launcher::onError
            }
            properties {
                format = "Hocon"
            }
            services {
               rest {}
            }
        }.start()
    }
    
    fun beforeStart(service: BaseMicroService) {
        (service.bean(ConfigParams::class.java))!!.log()
        log().info("${service.name} is started")
    }
    
    fun afterStop(service: BaseMicroService) {
        log().info("${service.name} is stopped")
    }

    fun onError(@Suppress("UNUSED_PARAMETER") service: BaseMicroService, exception: Exception) {
        log().error(exception.message, exception)
    }
    

}

