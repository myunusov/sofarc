@file:Suppress("unused")

package org.maxur.sofarc.core.embedded

import org.glassfish.hk2.api.Factory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Inject

@Service
class ServiceProvider
    @Inject constructor(
            val config: AllServiceConfig,
            val locator: Locator
        ) : Factory<EmbeddedService> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ServiceProvider::class.java)
    }

    private val nullService = object : EmbeddedService() {
        override fun start() = Unit
        override fun stop() = Unit
    }

    lateinit var service: EmbeddedService

    @PostConstruct
    fun init() {
        val services = config.serviceConfigs.map({ findService(it)}).filterNotNull()
        when (services.size) {
             0 -> service = nullService
             1 -> service = services[0]
            else -> service = CompositeService(services)
        }
    }

    private fun  findService(cfg: ServiceConfig): EmbeddedService? {
        when (cfg) {
            is ServiceProxy -> return cfg.service
            is ServiceDescriptor<*> -> return embeddedService(cfg as ServiceDescriptor<Any>)
        }
        return null
    }

    private fun embeddedService(cfg: ServiceDescriptor<Any>): EmbeddedService? {
        @Suppress("UNCHECKED_CAST")
        val clazz = EmbeddedServiceFactory::class.java
        val factory = locator.service(clazz, cfg.type) as EmbeddedServiceFactory<Any>?
        if (factory == null) {
            onError(cfg)
            return null
        }
        val result = factory.make(cfg)
        return if (result == null) {
            log.info("Service '${cfg.type}' is not configured\n")
            null
        } else {
            log.info("Service '${factory.name}' is configured\n")
            result
        }
    }

    private fun makeNullService(): EmbeddedService = nullService

    private fun onError(cfg: ServiceDescriptor<Any>) {
        val list = locator.names(EmbeddedServiceFactory::class.java)
        throw IllegalStateException(
                "Service '${cfg.type}' is not supported. Try one from this list: $list"
        )
    }

    override fun dispose(instance: EmbeddedService?) = Unit
    override fun provide(): EmbeddedService = service

    class CompositeService(val services: List<EmbeddedService>) : EmbeddedService() {

        override fun start() = services.forEach( { it.start() })

        override fun stop() = services.reversed().forEach( { it.stop() })

    }

}


