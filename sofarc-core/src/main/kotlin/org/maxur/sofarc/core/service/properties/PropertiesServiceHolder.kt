package org.maxur.sofarc.core.service.properties

import org.glassfish.hk2.api.IterableProvider
import org.glassfish.hk2.api.ServiceLocator
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.PropertiesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Inject

@Service
class PropertiesServiceHolder @Inject constructor(
        val configSource: ConfigSource,
        val propertiesServices: IterableProvider<PropertiesService>,
        val locator: ServiceLocator
) {
    
    lateinit var propertiesService: PropertiesService

    companion object {
        val log: Logger = LoggerFactory.getLogger(PropertiesServiceHolder::class.java)
    }

    @PostConstruct
    fun init() {
        val formatName = configSource.format
        val service: PropertiesService? = propertiesServices.named(formatName).get()
        if (service == null) {
            val list =
                    this.locator.getAllServiceHandles(PropertiesService::class.java)
                            .map({ it.activeDescriptor.name })
            throw IllegalStateException(
                    "Properties service '$formatName' is not supported. Try one from this list: $list"
            )
        }
        propertiesService = service
        log.info("Configuration Properties Service is '$formatName'\n")
    }

}

