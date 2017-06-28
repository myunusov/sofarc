@file:Suppress("unused")

package org.maxur.sofarc.core

import org.maxur.sofarc.core.embedded.EmbeddedService
import java.util.concurrent.Executors


/**
 * The micro-service
 *
 * @param service Embedded service (may be composite)
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
class MicroService constructor(
        val service: EmbeddedService,
        val locator: Locator
) {

    private var state: State = State.STOP

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                this@MicroService.stop()
            }
        })
    }

    /**
     * The Service Name
     */
    var name: String = "Anonymous microService"

    /**
     * on start event
     */
    var beforeStart: ((MicroService) -> Unit)? = null

    /**
     * on stop event
     */
    var afterStop: ((MicroService) -> Unit)? = null

    /**
     * on error event
     */
    var onError: ((MicroService, Exception) -> Unit)? = null

    fun <T> bean(clazz: Class<T>): T? = locator.service(clazz)
    
    /**
     * Start Service
     */
    fun start() {
        if (state == State.START) return
        try {
            beforeStart?.invoke(this)
            service.start()
            state =  State.START
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Stop Service
     */
    fun deferredStop() {
        if (state == State.STOP) return
        try {
            postpone({
                service.stop()
                afterStop?.invoke(this)
                state =  State.STOP
            })
        } catch(e: Exception) {
            error(e)
        }
    }

    fun stop() {
        if (state == State.STOP) return
        try {
            service.stop()
            afterStop?.invoke(this)
            state =  State.STOP
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Restart Service
     */
    fun deferredRestart() {
        if (state == State.STOP) return
        try {
            postpone({
                service.stop()
                afterStop?.invoke(this)
                state =  State.STOP
                beforeStart?.invoke(this)
                service.start()
                state = State.START
            })
        } catch(e: Exception) {
            error(e)
        }
    }

    fun error(exception: Exception) {
        onError?.invoke(this, exception)
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
         *  Running application
         */
        START,

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

