@file:Suppress("unused")

package org.maxur.sofarc.core.service.properties

import org.glassfish.hk2.api.Factory
import org.glassfish.hk2.api.IterableProvider
import org.jvnet.hk2.annotations.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * On multi PropertiesSources you can implement composite of PropertiesService
 */
@Service
class PropertiesServiceProvider
    @Inject constructor(
            val factories: IterableProvider<PropertiesServiceFactory>,
            val source: PropertiesSource
        ) : Factory<PropertiesService> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PropertiesServiceProvider::class.java)
    }

    lateinit var service: PropertiesService

    @PostConstruct
    fun init() {
        val formatName = source.format
        val factory: PropertiesServiceFactory? = factories.named(formatName).get()
        if (factory == null) {
            return onError(formatName)
        }
        val make = factory.make(source)
        service = if (make != null) {
            log.info("Properties Service is '${factory.name}'\n")
            make
        }
        else {
            log.info("Properties Service is not configured\n")
            NullPropertiesService()
        }
    }

    private fun onError(formatName: String) {
        val list = factories.map { it.name}
        throw IllegalStateException(
                "Properties service '$formatName' is not supported. Try one from this list: $list"
        )
    }

    override fun dispose(instance: PropertiesService?) = Unit
    override fun provide(): PropertiesService = service

}

