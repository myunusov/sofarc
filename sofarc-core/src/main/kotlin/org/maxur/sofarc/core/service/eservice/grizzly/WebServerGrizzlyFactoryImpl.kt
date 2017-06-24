package org.maxur.sofarc.core.service.eservice.grizzly

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.Locator
import org.maxur.sofarc.core.service.eservice.EmbeddedService
import org.maxur.sofarc.core.service.eservice.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.eservice.ServiceConfig
import org.maxur.sofarc.core.service.eservice.grizzly.properties.WebAppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
@Suppress("unused")
@Service(name = "Grizzly")
class WebServerGrizzlyFactoryImpl
@Inject constructor(
        val config: RestResourceConfig,
        val locator: Locator
) : EmbeddedServiceFactory<WebAppProperties>() {

    override fun make(cfg: ServiceConfig.LookupDescriptor): EmbeddedService<WebAppProperties>? {
        if (cfg.properties != null) {
            return WebServerGrizzlyImpl(cfg.properties as WebAppProperties, config, locator)
        } else if (cfg.propertyKey != null) {
            return WebServerGrizzlyImpl(cfg.propertyKey, config, locator)
        }
        return null
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServerGrizzlyFactoryImpl::class.java)
    }


}