package org.maxur.sofarc.core.service

import org.glassfish.hk2.api.ActiveDescriptor
import org.glassfish.hk2.api.IterableProvider
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.BuilderHelper
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.service.hk2.ConfigurationInjectionResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Inject

@Service
class PropertiesServiceHolder @Inject constructor(
        val propertiesServices: IterableProvider<PropertiesService>,
        val locator: ServiceLocator
) {
    
    lateinit var propertiesService: PropertiesService

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigurationInjectionResolver::class.java)
    }

    @PostConstruct
    fun init() {
        if (propertiesServices.size == 1) {
            propertiesService = propertiesServices.get()
            val filter = BuilderHelper.createContractFilter(PropertiesService::class.java.name)
            val descriptors: List<ActiveDescriptor<*>> = locator.getDescriptors(filter)
            log.info("Configuration Properties Service is '${descriptors.get(0).name}'")
        } else {
            throw IllegalStateException("More than one Configuration Properties Service is found")
        }
    }
}

