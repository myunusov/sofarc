@file:Suppress("unused")

package org.maxur.sofarc.core.embedded

import org.glassfish.hk2.api.Factory
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import javax.annotation.PostConstruct
import javax.inject.Inject

@Service
class ServiceProvider
@Inject constructor(
        val config: MicroServiceConfig,
        val locator: Locator
) : Factory<EmbeddedService> {

    private val nullService = object : EmbeddedService() {
        override fun start() = Unit
        override fun stop() = Unit
    }

    lateinit var service: EmbeddedService
    
    override fun dispose(instance: EmbeddedService?) = Unit
    override fun provide(): EmbeddedService = service

    @PostConstruct
    fun init() {
        val services = config.serviceConfigs.map({ it.service(locator) }).filterNotNull()
        when (services.size) {
            0 -> service = nullService
            1 -> service = services[0]
            else -> service = CompositeService(services)
        }
    }

    class CompositeService(val services: List<EmbeddedService>) : EmbeddedService() {
        override fun start() = services.forEach({ it.start() })
        override fun stop() = services.reversed().forEach({ it.stop() })
    }

}


