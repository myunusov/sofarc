@file:Suppress("unused")

package org.maxur.sofarc.core.service.embedded

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

    private fun findService(cfg: ServiceConfig): EmbeddedService? {
        when (cfg.descriptor) {
            is ServiceConfig.DirectDescriptor -> return cfg.descriptor.service
            is ServiceConfig.LookupDescriptor -> return embeddedService(cfg.descriptor)
        }
        return null
    }

    private fun embeddedService(cfg: ServiceConfig.LookupDescriptor): EmbeddedService? {
        @Suppress("UNCHECKED_CAST")
        val factory = locator.service(cfg.clazz, cfg.type) as EmbeddedServiceFactory<Any>?
        if (factory == null) {
            onError(cfg)
            return null
        }
        val result = factory.make(cfg)
        return if (result == null) {
            log.info("Service '${cfg.type}' is not configured\n")
            null
        } else if (cfg.clazz.isAssignableFrom(result::class.java)) {
            log.info("Service '${cfg.type}' has wrong type\n")
            null
        } else {
            log.info("Service '${factory.name}' is configured\n")
            result
        }
    }

    private fun makeNullService(): EmbeddedService = nullService

    private fun onError(cfg: ServiceConfig.LookupDescriptor) {
        val list = locator.names(cfg.clazz)
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


