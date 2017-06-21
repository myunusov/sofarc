package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.maxur.sofarc.core.service.Config
import org.maxur.sofarc.core.service.MicroService
import org.maxur.sofarc.core.service.PropertiesServiceHolder
import org.maxur.sofarc.core.service.WebServer
import org.slf4j.LoggerFactory

object DSL {

    private fun log() = LoggerFactory.getLogger(DSL::class.java)

    private lateinit var locator: ServiceLocator

    fun newLocator(vararg binders: Binder): ServiceLocator {
        locator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        ServiceLocatorUtilities.bind(locator, Config(), *binders)
        return locator.getService<ServiceLocator>(ServiceLocator::class.java)
    }

    fun service(vararg binders: Binder): MicroService {
        try {
            return newLocator(*binders).getService<MicroService>(MicroService::class.java)
        } catch (e:Exception) {
            log().error("Application is not configured")
            throw IllegalStateException("Application is not configured", e)
        }
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

    fun property(key: String): () -> String {
        return {
            this.locator
                    .getService<PropertiesServiceHolder>(PropertiesServiceHolder::class.java)
                    .propertiesService.asString(key)!!
        }
    }

    fun <T> hoconConfigTo(result: Class<T>): T {
        return this.locator.getService<T>(result)
    }

}