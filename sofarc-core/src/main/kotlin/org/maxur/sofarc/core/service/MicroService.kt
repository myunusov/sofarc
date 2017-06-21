@file:Suppress("unused")

package org.maxur.sofarc.core.service

import org.jvnet.hk2.annotations.Service
import java.util.concurrent.Executors
import javax.inject.Inject


/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class MicroService @Inject constructor() {

    private var nameFunc: () -> String = { "Unknown" }

    private var configFunc: () -> Any = { Unit }

    private var servicesFuncs: MutableList<() -> EmbeddedService> = mutableListOf()

    lateinit var name: String

    lateinit var config: Any

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

    fun beforeStart(func : (MicroService) -> Unit): MicroService {
        beforeStart = func
        return this
    }

    fun afterStop(func : (MicroService) -> Unit): MicroService {
        afterStop = func
        return this
    }

    fun onError(func : (MicroService, Exception) -> Unit): MicroService {
        onError = func
        return this
    }
    fun name(value: String): MicroService {
        nameFunc = { value }
        return this
    }

    fun name(func: () -> String): MicroService {
        nameFunc = func
        return this
    }

    fun embedded(vararg value: EmbeddedService): MicroService {
        value.forEach { servicesFuncs.add( { it } ) }
        return this
    }

    fun embedded(func: () -> EmbeddedService): MicroService {
        servicesFuncs.add(func)
        return this
    }

    fun <T> config(value: T): MicroService {
        configFunc = { value!! }
        return this
    }

    fun <T> config(func: () -> T): MicroService {
        @Suppress("UNCHECKED_CAST")
        configFunc =  func as () -> Any
        return this
    }

    fun <T> config(): T {
        @Suppress("UNCHECKED_CAST")
       return config as T
    }
    
    /**
     * Start Service
     */
    fun start() {
        name = nameFunc.invoke()
        config = configFunc.invoke()
        services =  servicesFuncs.map { it.invoke() }
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
