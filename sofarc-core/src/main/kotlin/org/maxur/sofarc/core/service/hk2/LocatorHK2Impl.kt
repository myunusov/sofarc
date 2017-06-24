package org.maxur.sofarc.core.service.hk2

import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.Locator
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import org.maxur.sofarc.core.service.properties.PropertiesService
import org.maxur.sofarc.core.service.properties.PropertiesSource
import javax.inject.Singleton

class LocatorHK2Impl : Locator() {

    val locator: ServiceLocator by lazy {
         ServiceLocatorUtilities.createAndPopulateServiceLocator()
    }

    override fun property(key: String): String = locator.getService(PropertiesService::class.java).asString(key)!!
    
    override fun <T> service(clazz: Class<T>): T? = locator.getService<T>(clazz)

    override fun <T> service(clazz: Class<T>, name: String): T? = locator.getService<T>(clazz)

    override fun names(clazz: Class<*>): List<String> =
            locator.getAllServiceHandles(WebServer::class.java).map({ it.activeDescriptor.name })

    override fun bind(propertiesSource: PropertiesSource, vararg binders: Binder) {
        ServiceLocatorUtilities.bind(locator, ObjectMapperBinder(), ConfigSourceBinder(propertiesSource), *binders)
    }

    private class ConfigSourceBinder(val propertiesSource: PropertiesSource) : AbstractBinder() {
        override fun configure() {
            bind(propertiesSource).to(PropertiesSource::class.java)
            bind(PropertiesInjectionResolver::class.java)
                    .to(object : TypeLiteral<InjectionResolver<Value>>() {})
                    .`in`(Singleton::class.java)
        }
    }

    private class ObjectMapperBinder : AbstractBinder() {
        override fun configure() {
            bindFactory(ObjectMapperProvider::class.java)
                    .to(ObjectMapper::class.java)
                    .`in`(Singleton::class.java)
        }
    }


}


