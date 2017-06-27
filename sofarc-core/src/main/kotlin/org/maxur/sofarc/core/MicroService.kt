@file:Suppress("unused")

package org.maxur.sofarc.core

import org.glassfish.hk2.utilities.Binder
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.service.hk2.Builder
import org.maxur.sofarc.core.service.hk2.MicroServiceBuilder
import java.util.concurrent.Executors
import javax.inject.Inject


/**
 * The microservice
 *
 * @param service Embedded service (may be composite)
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class MicroService @Inject constructor(val service: EmbeddedService) {

    companion object {
        fun service(vararg binders: Binder): Builder = MicroServiceBuilder(*binders)
        fun restService(vararg binders: Binder): Builder =
                MicroServiceBuilder(*binders).properties().web()
    }

    /**
     * The Service Locator
     */
    lateinit var locator: Locator

    /**
     * The Service Name
     */
    lateinit var name: String

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

    fun <T> bean(clazz: Class<T>): T? = locator.service(clazz)
    
    /**
     * Start Service
     */
    fun start() {
        try {
            beforeStart.invoke(this)
            service.start()
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Stop Service
     */
    fun deferredStop() {
        try {
            postpone({
                service.stop()
                afterStop.invoke(this)
            })
        } catch(e: Exception) {
            error(e)
        }
    }

    fun stop() {
        try {
            service.stop()
            afterStop.invoke(this)
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Restart Service
     */
    fun deferredRestart() {
        try {
            postpone({
                service.stop()
                afterStop.invoke(this)
                beforeStart.invoke(this)
                service.start()
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