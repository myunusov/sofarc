@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.MicroService
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.embedded.MicroServiceConfig
import org.maxur.sofarc.core.embedded.ServiceConfig
import org.maxur.sofarc.core.service.properties.PropertiesSource
import java.util.*

interface Builder {

    fun beforeStart(func: (MicroService) -> Unit): Builder

    fun afterStop(func: (MicroService) -> Unit): Builder

    fun onError(func: (MicroService, Exception) -> Unit): Builder

    fun name(value: String): Builder

    fun embed(vararg value: EmbeddedService): Builder

    fun embed(value: String): MicroServiceBuilder.ServiceBuilder

    fun web(): Builder

    fun properties(): MicroServiceBuilder.PropertiesSourceBuilder

    /**
     * Start Service
     */
    fun start()
}

class MicroServiceBuilder(vararg binders: Binder): Builder {
    @Suppress("CanBePrimaryConstructorProperty")
    private val binders: Array<out Binder> = binders

    private val propertiesSourceBuilder: PropertiesSourceBuilder = PropertiesSourceBuilder(this)

    private var nameHolder: Holder<String> = Holder.string("Anonymous")

    private var serviceBuilder: ServiceBuilder = ServiceBuilder(this)

    private var beforeStart: (MicroService) -> Unit = {}

    private var afterStop: (MicroService) -> Unit = {}

    private var onError: (MicroService, Exception) -> Unit = { _, _ -> }

    override fun beforeStart(func: (MicroService) -> Unit): MicroServiceBuilder {
        beforeStart = func
        return this
    }

    override fun afterStop(func: (MicroService) -> Unit): MicroServiceBuilder {
        afterStop = func
        return this
    }

    override fun onError(func: (MicroService, Exception) -> Unit): MicroServiceBuilder {
        onError = func
        return this
    }

    override fun name(value: String): MicroServiceBuilder {
        nameHolder = Holder.string(value)
        return this
    }

    override fun embed(vararg value: EmbeddedService): MicroServiceBuilder {
        value.forEach {
            serviceBuilder.add(it)
        }
        return this
    }

    override fun embed(value: String): ServiceBuilder {
        serviceBuilder.add(value)
        return serviceBuilder
    }

    override fun web(): Builder {
        serviceBuilder.add("Grizzly")
        serviceBuilder.propertiesKey(":webapp")
        return serviceBuilder
    }

    override fun properties(): PropertiesSourceBuilder {
        return propertiesSourceBuilder
    }

    /**
     * Start Service
     */
    override fun start() {
        build().start()
    }

    fun build(): MicroService {
        val locator = LocatorFactoryHK2Impl()
                .bind(serviceBuilder.build())
                .bind(propertiesSourceBuilder.build())
                .bind(*binders)
                .make()

        val service = locator.service(MicroService::class.java) ?:
                throw IllegalStateException("MicroService is not configured")

        service.locator = locator
        service.name = nameHolder.get(locator)!!
        service.beforeStart = beforeStart
        service.afterStop = afterStop
        service.onError = onError
        return service
    }

    class PropertiesSourceBuilder(val parent: MicroServiceBuilder): Builder by parent {

        var format: String = "Hocon"

        var rootKey: String = "DEFAULTS"

        fun fromClasspath(): PropertiesSourceBuilder = this

        fun format(value: String): PropertiesSourceBuilder {
            format = value
            return this
        }

        fun rootKey(value: String): PropertiesSourceBuilder {
            rootKey = value
            return this
        }

        override fun properties(): PropertiesSourceBuilder {
            return this
        }

        fun build(): PropertiesSource {
            return PropertiesSource(format, rootKey)
        }
    }

    class ServiceBuilder(val parent: MicroServiceBuilder): Builder by parent {

        val configs = ArrayList<ServiceConfig>()

        var isPresent: Boolean = false

        lateinit var type: String

         var properties : Holder<Any?> = Holder.wrap(null)

        fun properties(value: Any): ServiceBuilder {
            properties = Holder.wrap(value)
            return this
        }

        fun propertiesKey(value: String): ServiceBuilder {
            val key: String = if (value.startsWith(":"))
                value.substringAfter(":")
            else
                throw IllegalArgumentException("A Key Name must be started with ':'")
            properties = Holder.get<Any?> { locator, clazz -> locator.properties(key, clazz)!! }
            return this
        }

        fun build(): MicroServiceConfig {
            complete()
            return MicroServiceConfig(Collections.unmodifiableCollection(configs))
        }
        
        fun add(service: EmbeddedService) {
            complete()
            configs.add(ServiceConfig(service))
            isPresent = false
        }

        fun add(value: String) {
            complete()
            type = value
            isPresent = true
        }

        private fun complete() {
            if (isPresent) {
                configs.add(ServiceConfig(type, properties))
            }
        }

    }


}

