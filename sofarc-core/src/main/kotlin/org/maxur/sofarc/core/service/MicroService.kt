@file:Suppress("unused")

package org.maxur.sofarc.core.service

import org.apache.shiro.SecurityUtils
import org.apache.shiro.config.IniSecurityManagerFactory
import org.jvnet.hk2.annotations.Service
import java.util.concurrent.Executors




/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class MicroService {

    /**
     * The Service Name
     */
    lateinit var name: String

    /**
     * The Service Config
     */
    lateinit var config: Any

    /**
     * List of embedded services
     */
    lateinit var services: List<EmbeddedService>

    /**
     * on start event
     */
    lateinit var beforeStart: (MicroService) -> Unit

    /**
     * on stop event
     */
    lateinit var afterStop: (MicroService) -> Unit

    /**
     * on error event
     */
    lateinit var onError: (MicroService, Exception) -> Unit

    /**
     * Start Service
     */
    fun start() {
        val factory = IniSecurityManagerFactory("classpath:shiro.ini")
        val securityManager = factory.instance
        SecurityUtils.setSecurityManager(securityManager)
        try {
            beforeStart.invoke(this)
            services.forEach { it.start() }
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Stop Service
     */
    fun stop() {
        try {
            postpone({
                services.forEach { it.stop() }
                afterStop.invoke(this)
            })
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Restart Service
     */
    fun restart() {
        try {
            postpone({
                services.forEach { it.stop() }
                afterStop.invoke(this)
                beforeStart.invoke(this)
                services.forEach { it.start() }
            })
        } catch(e: Exception) {
            error(e)
        }
    }

    fun error(exception: Exception) {
        onError.invoke(this, exception)
    }


    private fun postpone(func: () -> Unit) {
        val pool = Executors.newSingleThreadExecutor { runnable ->
            val thread = Executors.defaultThreadFactory().newThread(runnable)
            thread.isDaemon = false
            thread
        }
        pool.submit {
            Thread.sleep(1000)
            func.invoke()
        }
        pool.shutdown()
    }


    /**
     * Represent State of micro-service
     */
    enum class State {

        /**
         * Stop application
         */
        STOP,

        /**
         * Restart application
         */
        RESTART;

        companion object {

            fun from(value: String): State {
                val case = value.toUpperCase()
                if (case in State::class.java.enumConstants.map { e -> e.name }) {
                    return State.valueOf(case)
                } else {
                    throw IllegalArgumentException("The '$value' is not acceptable Application State")
                }
            }
        }

    }

}
