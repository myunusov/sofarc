package org.maxur.sofarc.core.service.embedded.grizzly

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.rest.RestResourceConfig
import org.maxur.sofarc.core.service.embedded.EmbeddedService
import org.maxur.sofarc.core.service.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.embedded.ServiceConfig
import org.maxur.sofarc.core.service.embedded.properties.WebAppProperties
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

    companion object {
        val log: Logger = LoggerFactory.getLogger(WebServerGrizzlyFactoryImpl::class.java)
    }

    override fun make(cfg: ServiceConfig.LookupDescriptor): EmbeddedService? {
        val properties: WebAppProperties? =
                when {
                    cfg.properties != null -> cfg.properties as WebAppProperties
                    cfg.propertyKey != null -> properties(cfg.propertyKey, locator)
                    else -> null
                }
        return  WebServerGrizzlyImpl(properties, config, locator)
    }



}