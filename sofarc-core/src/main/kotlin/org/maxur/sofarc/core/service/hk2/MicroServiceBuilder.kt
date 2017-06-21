@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.EmbeddedService
import org.maxur.sofarc.core.service.MicroService

class MicroServiceBuilder(vararg binders: Binder) {

    @Suppress("CanBePrimaryConstructorProperty")
    private val binders: Array<out Binder> = binders

    private var nameFunc: () -> String = { "Unknown" }

    private var formatFunc: () -> ConfigSource = { ConfigSource("Hocon") }

    private var configFunc: () -> Any = { Unit }

    private var servicesFuncs: MutableList<() -> EmbeddedService> = mutableListOf()

    private var beforeStart: (MicroService) -> Unit = {}

    private var afterStop: (MicroService) -> Unit = {}

    private var onError: (MicroService, Exception) -> Unit = { microService: MicroService, exception: Exception -> }

    fun beforeStart(func: (MicroService) -> Unit): MicroServiceBuilder {
        beforeStart = func
        return this
    }

    fun afterStop(func: (MicroService) -> Unit): MicroServiceBuilder {
        afterStop = func
        return this
    }

    fun onError(func: (MicroService, Exception) -> Unit): MicroServiceBuilder {
        onError = func
        return this
    }

    fun name(value: String): MicroServiceBuilder {
        nameFunc = { value }
        return this
    }

    fun name(func: () -> String): MicroServiceBuilder {
        nameFunc = func
        return this
    }

    fun embedded(vararg value: EmbeddedService): MicroServiceBuilder {
        value.forEach { servicesFuncs.add({ it }) }
        return this
    }

    fun embedded(func: () -> EmbeddedService): MicroServiceBuilder {
        servicesFuncs.add(func)
        return this
    }

    fun <T> configTo(value: T): MicroServiceBuilder {
        configFunc = { value!! }
        return this
    }

    fun <T> configTo(func: () -> T): MicroServiceBuilder {
        @Suppress("UNCHECKED_CAST")
        configFunc = func as () -> Any
        return this
    }

    fun configFrom(value: ConfigSource): MicroServiceBuilder {
        formatFunc = { value }
        return this
    }

    fun configFrom(func: () -> ConfigSource): MicroServiceBuilder {
        formatFunc = func
        return this
    }

    /**
     * Start Service
     */
    fun start() {
        val locator = DSL.newLocator(formatFunc.invoke(), *binders)
        val service = locator.getService<MicroService>(MicroService::class.java)
        service.name = nameFunc.invoke()
        service.config = configFunc.invoke()
        service.services = servicesFuncs.map { it.invoke() }
        service.beforeStart = beforeStart
        service.afterStop = afterStop
        service.onError = onError
        service.start()
    }


}