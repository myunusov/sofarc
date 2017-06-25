@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.MicroService
import org.maxur.sofarc.core.embedded.*
import org.maxur.sofarc.core.service.properties.PropertiesSource
import java.util.*

interface Builder {

    fun beforeStart(func: (MicroService) -> Unit): Builder

    fun afterStop(func: (MicroService) -> Unit): Builder

    fun onError(func: (MicroService, Exception) -> Unit): Builder

    fun name(value: String): Builder

    fun embed(vararg value: EmbeddedService): Builder

    fun embed(value: String): MicroServiceBuilder.ServiceBuilder

    fun config(): MicroServiceBuilder.PropertiesSourceBuilder

    /**
     * Start Service
     */
    fun start()
}

class MicroServiceBuilder(vararg binders: Binder): Builder {

    @Suppress("CanBePrimaryConstructorProperty")
    private val binders: Array<out Binder> = binders

    private val propertiesSourceBuilder: PropertiesSourceBuilder = PropertiesSourceBuilder(this)

    private var nameBuilder: PropertyBuilder = PropertyBuilder("Anonymous")

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
        nameBuilder = PropertyBuilder(value)
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

    override fun config(): PropertiesSourceBuilder {
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
        service.name = nameBuilder.build(locator)
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

        override fun config(): PropertiesSourceBuilder {
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

        // TODO
        var clazz: Class<out EmbeddedServiceFactory<Any>> =
                EmbeddedServiceFactory::class.java as Class<out EmbeddedServiceFactory<Any>>
        // TODO
        var propertyKey : String = "webapp"

        fun build(): AllServiceConfig {
            if (isPresent) {
                configs.add(ServiceDescriptor<Any>(type, propertyKey, null))
            }
            return AllServiceConfig(Collections.unmodifiableCollection(configs))
        }

        constructor(parent: MicroServiceBuilder, propertyKey: String) : this(parent) {
            this.propertyKey = propertyKey
        }


        fun add(service: EmbeddedService) {
            if (isPresent) {
                configs.add(ServiceDescriptor<Any>(type, propertyKey, null))
            }
            configs.add(ServiceProxy(service))
            isPresent = false
        }

        fun add(value: String) {
            if (isPresent) {
                configs.add(ServiceDescriptor<Any>(type, propertyKey, null))
            }
            isPresent = true
            type = value
        }

    }

    class PropertyBuilder(val value: String) {

        fun build(locator: Locator): String =
                when {
                    value.startsWith(":") -> locator.property(value.substringAfter(":"))
                    else -> value
                }
    }

}

