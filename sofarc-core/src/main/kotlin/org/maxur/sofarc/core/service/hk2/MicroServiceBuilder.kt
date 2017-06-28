@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.MicroService
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MicroServiceBuilder()  {

    constructor(init: MicroServiceBuilder.() -> Unit) : this() {
        init()
    }

    private var locatorHolder: LocatorHolder? = null
    private var observersHolder: ObserversHolder? = null
    private var titleHolder: Holder<String> = Holder.string("Anonymous")
    private var propertiesHolder: PropertiesHolder = PropertiesHolder()
    private var servicesHolder: ServicesHolder = ServicesHolder()

    var title : String = "Anonymous"
        set(value) {
            titleHolder = Holder.string(value)
        }

    fun binders(init: LocatorHolder.() -> Unit) {
        locatorHolder = LocatorHolder().apply { init() }
    }

    fun observers(init: ObserversHolder.() -> Unit) {
        observersHolder = ObserversHolder().apply { init() }
    }

    fun properties(init: PropertiesHolder.() -> Unit) {
        propertiesHolder = PropertiesHolder().apply { init() }
    }

    fun services(init: ServicesHolder.() -> Unit) {
        servicesHolder = ServicesHolder().apply { init() }
    }

    fun build(): MicroService {

        val locatorFactory = LocatorFactoryHK2Impl()
        locatorHolder?.apply { locatorFactory.bind(*binders) }
        locatorFactory.bind(propertiesHolder.build())
        val locator = locatorFactory.make()
        val service = MicroService(servicesHolder.build(locator), locator)
        // TODO
        val binder =  object: AbstractBinder() {
            override fun configure() {
                bind(service).to(MicroService::class.java)
            }
        }  
        val implementation: ServiceLocator = locator.implementation<ServiceLocator>()
        ServiceLocatorUtilities.bind(implementation, binder)
        service.name = titleHolder.get(locator)!!
        service.beforeStart = observersHolder?.beforeStart
        service.afterStop = observersHolder?.afterStop
        service.onError = observersHolder?.onError
        return service
    }
}

private val nullService = object : EmbeddedService() {
    override fun start() = Unit
    override fun stop() = Unit
}

class ServicesHolder {

    private var serviceHolder: ArrayList<ServiceHolder> = ArrayList()

    fun service(init: ServiceHolder.() -> Unit) {
        serviceHolder.add(ServiceHolder().apply { init() })
    }

    fun rest(init: ServiceHolder.() -> Unit) {
        serviceHolder.add(ServiceHolder().apply {
            type = "Grizzly"
            properties = ":webapp"
            init()
        })
    }

    fun build(locator: Locator): EmbeddedService =
            CompositeService(serviceHolder.map { it.service(locator)!! })

}

class CompositeService(val services: List<EmbeddedService>) : EmbeddedService() {
    override fun start() = services.forEach({ it.start() })
    override fun stop() = services.reversed().forEach({ it.stop() })
}

class ServiceHolder {

    private var serviceHolder: Holder<EmbeddedService?> = Holder.wrap(nullService)
    private var propertiesHolder: Holder<Any?> = Holder.wrap<Any?>(null)
    private var typeHolder: String? = null

    var ref: EmbeddedService
        get() {
            throw UnsupportedOperationException()
        }
        set(value) {
            this.propertiesHolder = Holder.wrap<Any?>(null)
            this.serviceHolder = Holder.wrap<EmbeddedService?>(value)
            this.typeHolder = null
        }

    var type: String
        get() {
            throw UnsupportedOperationException()
        }
        set(value) {
            this.typeHolder = value
            this.serviceHolder = Holder.get{
                locator -> ServiceFactory(value, propertiesHolder).make(locator)
            }
        }

    var properties: Any
        get() {
            throw UnsupportedOperationException()
        }
        set(value) {
            this.propertiesHolder = when(value) {
                is String -> propertiesKey(value)
                else -> Holder.wrap(value)
            }
            this.serviceHolder = Holder.get{
                locator -> ServiceFactory(typeHolder ?: "unknown", propertiesHolder).make(locator)
            }
        }

    private fun propertiesKey(value: String): Holder<Any?> {
        val key: String = if (value.startsWith(":"))
            value.substringAfter(":")
        else
            throw IllegalArgumentException("A Key Name must be started with ':'")
        return Holder.get<Any?> { locator, clazz -> locator.properties(key, clazz)!! }
    }


    fun service(locator: Locator): EmbeddedService? = serviceHolder.get<EmbeddedService>(locator)

}

class ServiceFactory(val name: String, private val holder: Holder<Any?>) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ServiceFactory::class.java)
    }

    fun make(locator: Locator): EmbeddedService? {
        val clazz = EmbeddedServiceFactory::class.java
        val factory = locator.service(clazz, name) ?: return serviceNotFoundError(locator)
        return factory.make(holder)
                .let { success(factory, it) } ?:
                serviceNotCreatedError()
    }

    private fun serviceNotFoundError(locator: Locator): EmbeddedService? {
        val list = locator.names(EmbeddedServiceFactory::class.java)
        throw IllegalStateException(
                "Service '$name' is not supported. Try one from this list: $list"
        )
    }

    private fun success(factory: EmbeddedServiceFactory, result: EmbeddedService?): EmbeddedService? {
        log.info("Service '${factory.name}' is configured\n")
        return result
    }

    private fun serviceNotCreatedError(): Nothing? {
        log.info("Service '$name' is not configured\n")
        return null
    }

}

class LocatorHolder {
    var binders: Array<Binder> = arrayOf()
}

class ObserversHolder {
    var beforeStart: ((MicroService) -> Unit)? = null
    var afterStop: ((MicroService) -> Unit)? = null
    var onError: ((MicroService, Exception)  -> Unit)? = null
}

class PropertiesHolder {
    var format: String = "Hocon"
    var rootKey: String = "DEFAULTS"
    fun none() {
        format = "None"
        rootKey= ""
    }
    fun build() = PropertiesSource(format, rootKey)
}
