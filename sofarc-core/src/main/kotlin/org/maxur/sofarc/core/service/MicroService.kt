package org.maxur.sofarc.core.service

import org.jvnet.hk2.annotations.Contract
import java.util.*
import kotlin.concurrent.schedule

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
abstract class MicroService(val name: String, vararg val services: EmbeddedService) {

    /**
     * Start Service
     */
    fun start() {
        try {
            beforeStart()
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
            })
            afterStop()
        } catch(e: Exception) {
            error(e)
        }
    }

    /**
     * Restart Service
     */
    fun restart() {
        stop()
        start()
    }

    fun error(exception: Exception) {
        onError(exception)
    }


    private fun postpone(func: Function<Unit>) {
        Timer("schedule", true).schedule(100) {
            (func as () -> Unit).invoke()
        }
    }
    

    /**
     * on start event
     */
    abstract protected fun beforeStart()

    /**
     * on stop event
     */
    abstract protected fun afterStop()

    /**
     * on error event
     *
     * @param exception The exception associated with this failure
     */
    abstract protected fun onError(exception:Exception)

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
                if (case in State::class.java.getEnumConstants().map { e -> e.name }) {
                    return State.valueOf(case)
                } else {
                    throw IllegalArgumentException("The '$value' is not acceptable Application State")
                }
            }
        }

    }
}
