@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.BaseMicroService
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.MicroService
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesService
import org.maxur.sofarc.core.service.properties.PropertiesServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MicroServiceBuilder() {

    constructor(init: MicroServiceBuilder.() -> Unit) : this() {
        init()
    }

    private var locatorHolder: LocatorHolder = LocatorHolder()
    private var observersHolder: ObserversHolder? = null
    private var titleHolder: Holder<String> = Holder.string("Anonymous")
    private var propertiesHolder: PropertiesHolder = PropertiesHolder()
    private var servicesHolder: ServicesHolder = ServicesHolder()

    var title: String = "Anonymous"
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
        val locator = LocatorFactoryHK2Impl {
            bind(*locatorHolder.binders)
            bind(propertiesHolder::build, PropertiesService::class.java)
            bind({ locator -> BaseMicroService(servicesHolder.build(locator), locator) }, MicroService::class.java)
        }.make()

        val service = locator.service(MicroService::class.java) ?:
            throw IllegalStateException("A MicroService is not created. Maybe It's configuration is wrong")

        if (service is BaseMicroService) {
            service.name = titleHolder.get(locator)!!
            service.beforeStart = observersHolder?.beforeStart
            service.afterStop = observersHolder?.afterStop
            service.onError = observersHolder?.onError
        }
        return service
    }
}

class LocatorHolder {
    var binders: Array<Binder> = arrayOf()
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
        CompositeService(serviceHolder.map { it.build(locator)!! })

}

class CompositeService(val services: List<EmbeddedService>) : EmbeddedService() {
    override fun start() = services.forEach({ it.start() })
    override fun stop() = services.reversed().forEach({ it.stop() })
}

class ServiceHolder {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MicroServiceBuilder::class.java)
        private val clazz = EmbeddedServiceFactory::class.java
    }

    private var holder: Holder<EmbeddedService?> = Holder.none()
    private var propertiesHolder: Holder<Any?> = Holder.wrap(null)

    var ref: EmbeddedService? = null
        set(value) {
            this.propertiesHolder = Holder.none()
            this.holder = Holder.wrap(value)
        }

    var typeHolder: String? = null
    var type: String?
        get() = typeHolder
        set(value) {
            this.typeHolder = value
            this.holder = makeServiceHolder()
        }

    private fun makeServiceHolder(): Holder<EmbeddedService?> {
        return Holder.get {
            locator ->
            locator
                .locate(typeHolder ?: "unknown", clazz)
                .make(propertiesHolder)
                .apply { success() } ?: serviceNotCreatedError()
        }
    }

    var properties: Any? = null
        set(value) {
            this.propertiesHolder = when (value) {
                is String -> propertiesKey(value)
                else -> Holder.wrap(value)
            }
        }

    private fun success() {
        log.info("Service '$typeHolder' is configured\n")
    }

    private fun serviceNotCreatedError(): Nothing? {
        log.info("Service '$typeHolder' is not configured\n")
        return null
    }

    private fun propertiesKey(value: String): Holder<Any?> {
        val key: String = if (value.startsWith(":"))
            value.substringAfter(":")
        else
            throw IllegalArgumentException("A Key Name must be started with ':'")
        return Holder.get<Any?> { locator, clazz -> locator.properties(key, clazz)!! }
    }

    fun build(locator: Locator): EmbeddedService? = holder.get<EmbeddedService>(locator)

}

class ObserversHolder {
    var beforeStart: ((BaseMicroService) -> Unit)? = null
    var afterStop: ((BaseMicroService) -> Unit)? = null
    var onError: ((BaseMicroService, Exception) -> Unit)? = null
}

class PropertiesHolder {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MicroServiceBuilder::class.java)
        private val clazz = PropertiesServiceFactory::class.java
    }

    var format: String = "Hocon"
    var rootKey: String = "DEFAULTS"
    fun none() {
        format = "None"
        rootKey = ""
    }

    fun build(locator: Locator): PropertiesService {

        val source = PropertiesSource(format, rootKey)

        val factory: PropertiesServiceFactory = locator.locate(format, PropertiesServiceFactory::class.java)

        return factory.make(source)
            ?.apply { log.info("Properties Service is '${factory.name}'\n") }
            ?: throw IllegalStateException("Properties Service '$format' is not configured\n")
    }

}
