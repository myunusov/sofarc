package org.maxur.sofarc.core.service.hk2

import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.Locator
import org.maxur.sofarc.core.service.WebServer
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import javax.inject.Singleton

class LocatorHK2Impl : Locator() {

    val locator: ServiceLocator by lazy {
         ServiceLocatorUtilities.createAndPopulateServiceLocator()
    }

    override fun value(key: String): String {
        val propertiesService = locator.getService(PropertiesServiceHolder::class.java).propertiesService
        return propertiesService.asString(key)!!
    }
    
    override fun <T> service(clazz: Class<T>): T? = locator.getService<T>(clazz)

    override fun <T> service(clazz: Class<T>, name: String): T? = locator.getService<T>(clazz)

    override fun names(clazz: Class<*>): List<String> =
            locator.getAllServiceHandles(WebServer::class.java).map({ it.activeDescriptor.name })

    override fun bind(configSource: ConfigSource, vararg binders: Binder) {
        ServiceLocatorUtilities.bind(locator, ObjectMapperBinder(), ConfigSourceBinder(configSource), *binders)
    }

    private class ConfigSourceBinder(val configSource: ConfigSource) : AbstractBinder() {

        @Override
        override fun configure() {
            bind(configSource).to(ConfigSource::class.java)

            bind<ConfigurationInjectionResolver>(ConfigurationInjectionResolver::class.java)
                    .to(object : TypeLiteral<InjectionResolver<Value>>() {

                    })
                    .`in`(Singleton::class.java)
        }
    }

    private class ObjectMapperBinder : AbstractBinder() {
        @Override
        override fun configure() {
            bindFactory(ObjectMapperProvider::class.java).to(ObjectMapper::class.java).`in`(Singleton::class.java)
        }
    }


}


