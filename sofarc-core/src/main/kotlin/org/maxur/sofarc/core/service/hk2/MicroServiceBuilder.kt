@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.service.EmbeddedService
import org.maxur.sofarc.core.service.Locator
import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.core.service.properties.PropertiesSource

interface Builder {

    fun beforeStart(func: (MicroService) -> Unit): Builder

    fun afterStop(func: (MicroService) -> Unit): Builder

    fun onError(func: (MicroService, Exception) -> Unit): Builder

    fun name(value: String): Builder

    fun embed(vararg value: EmbeddedService): Builder

    fun embed(value: String): MicroServiceBuilder.ServiceBuilder

    fun config(): MicroServiceBuilder.ConfigSourceBuilder

    /**
     * Start Service
     */
    fun start()
}

class MicroServiceBuilder(vararg binders: Binder): Builder {

    @Suppress("CanBePrimaryConstructorProperty")
    private val binders: Array<out Binder> = binders

    private val configSourceBuilder: ConfigSourceBuilder = ConfigSourceBuilder(this)

    private var nameBuilder: PropertyBuilder = PropertyBuilder("Anonymous")

    private var serviceBuilders: MutableList<ServiceBuilder> = mutableListOf()

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
            serviceBuilders.add(ServiceBuilder(this, it))
        }
        return this
    }

    override fun embed(value: String): ServiceBuilder {
        val builder = ServiceBuilder(this, value)
        serviceBuilders.add(builder)
        return builder
    }

    override fun config(): ConfigSourceBuilder {
        return configSourceBuilder
    }

    /**
     * Start Service
     */
    override fun start() {
        build().start()
    }

    fun build(): MicroService {
        val locator: Locator = LocatorHK2Impl()
        locator.bind(configSourceBuilder.build(), *binders)

        val service = locator.service(MicroService::class.java) ?:
                throw IllegalStateException("MicroService is not configured")

        service.locator = locator
        service.name = nameBuilder.build(locator)
        service.services = serviceBuilders.map { it.build(locator) }
        service.beforeStart = beforeStart
        service.afterStop = afterStop
        service.onError = onError
        return service
    }

    class ConfigSourceBuilder(val parent: MicroServiceBuilder): Builder by parent {

        var format: String = "Hocon"

        var rootKey: String = "DEFAULTS"

        fun fromClasspath(): ConfigSourceBuilder = this

        fun format(value: String): ConfigSourceBuilder {
            format = value
            return this
        }

        fun rootKey(value: String): ConfigSourceBuilder {
            rootKey = value
            return this
        }

        fun build(): PropertiesSource {
            return PropertiesSource(format, rootKey)
        }

        override fun config(): ConfigSourceBuilder {
            return this
        }
    }

    class PropertyBuilder(val value: String) {
        fun build(locator: Locator): String =
                when {
                    value.startsWith(":") -> locator.property(value.substringAfter(":"))
                    else -> value
                }
    }

    class ServiceBuilder(
            val parent: MicroServiceBuilder,
            val func: (String, Class<out EmbeddedService>, Locator) -> EmbeddedService
    ): Builder by parent {

        lateinit var name: String
        var clazz: Class<out EmbeddedService> = EmbeddedService::class.java

        companion object {
              fun make(name: String, clazz: Class<out EmbeddedService>, locator: Locator): EmbeddedService {
                val result = locator.service(clazz, name)
                if (result == null) {
                    val list = locator.names(clazz)
                    throw IllegalStateException("Service '$name' is not supported. Try one from this list: $list")
                }
                return result
            }
        }

        fun configuredBy(key: String): Any {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


        fun asWebService(): MicroServiceBuilder {
            clazz = WebServer::class.java
            return parent
        }
        
        fun build(locator: Locator): EmbeddedService = func.invoke(name, clazz, locator)

        constructor(parent: MicroServiceBuilder, service: EmbeddedService) : this(parent, {_, _, _ -> service} )

        constructor(parent: MicroServiceBuilder, value: String) : this(parent, { n, c, l -> make(n, c, l) } ) {
            name = value
        }

    }

}

