package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.WebServer

object DSL {

    private lateinit var locator: ServiceLocator

    fun newLocator(configSource: ConfigSource, vararg binders: Binder): ServiceLocator {
        locator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        ServiceLocatorUtilities.bind(locator, Config(configSource), *binders)
        return locator.getService<ServiceLocator>(ServiceLocator::class.java)
    }

    fun service(vararg binders: Binder): MicroServiceBuilder {
        return MicroServiceBuilder(*binders)
    }

    fun webService(name: String): () -> WebServer {
        return {
            val webServer = this.locator.getService<WebServer>(WebServer::class.java, name)
            if (webServer == null) {
                val list = this.locator.getAllServiceHandles(WebServer::class.java).map({ it.activeDescriptor.name })
                throw IllegalStateException("Web service '$name' is not supported. Try one from this list: $list")
            }
            webServer
        }
    }

    fun cfg(key: String): () -> String {
        return {
            this.locator
                    .getService<PropertiesServiceHolder>(PropertiesServiceHolder::class.java)
                    .propertiesService.asString(key)!!
        }
    }


}