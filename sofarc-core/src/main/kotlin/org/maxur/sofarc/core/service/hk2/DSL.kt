package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.maxur.sofarc.core.domain.Factory
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.EmbeddedService
import org.maxur.sofarc.core.service.PropertiesService
import org.maxur.sofarc.core.service.WebServer

object DSL {

    private lateinit var locator: ServiceLocator

    fun newLocator(configSource: ConfigSource, vararg binders: Binder): ServiceLocator {
        locator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        ServiceLocatorUtilities.bind(locator, Config(configSource), *binders)
        return locator.getService<ServiceLocator>(ServiceLocator::class.java)
    }

    fun service(vararg binders: Binder): MicroServiceBuilder = MicroServiceBuilder(*binders)

    fun webService(name: String): WebServerDescription = WebServerDescription({ desc -> webServer(name) })

    private fun webServer(name: String): WebServer {
        val result = this.locator.getService<WebServer>(WebServer::class.java, name)
        if (result == null) {
            val list = this.locator.getAllServiceHandles(WebServer::class.java).map({ it.activeDescriptor.name })
            throw IllegalStateException("Web service '$name' is not supported. Try one from this list: $list")
        }
        return result
    }

    fun cfg(key: String): ConfigParam<String> = ConfigParam(String::class.java, key,
            {
                this.locator.getService<PropertiesServiceHolder>(PropertiesServiceHolder::class.java).propertiesService
            }
    )

}

class WebServerDescription(val creator: (WebServerDescription) -> WebServer) : Factory<EmbeddedService> {

    var key: String? = null

    override fun get() = creator.invoke(this).clone(key)

    fun withConfig(value: String): Factory<EmbeddedService> {
        key = value
        return this
    }
}

class ConfigParam<T>(val clazz: Class<T>, val key: String, val creator: () -> PropertiesService) : Factory<T> {

    @Suppress("UNCHECKED_CAST")
    override fun get() = creator.invoke().read(key, clazz)!! as T

    fun <Z> asClass(clazz: Class<Z>): ConfigParam<Z> = ConfigParam<Z>(clazz, key, creator)
    
}
