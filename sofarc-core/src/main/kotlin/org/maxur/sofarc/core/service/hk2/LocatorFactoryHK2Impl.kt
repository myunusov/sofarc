package org.maxur.sofarc.core.service.hk2

import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.embedded.MicroServiceConfig
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import org.maxur.sofarc.core.service.properties.PropertiesSource
import javax.inject.Singleton

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
class LocatorFactoryHK2Impl {

    val binders = ArrayList<Binder>()

    val locator: ServiceLocator by lazy {
        ServiceLocatorUtilities.createAndPopulateServiceLocator()
    }

    fun make(): Locator {
        binders.addAll(arrayListOf(LocatorBinder(), ObjectMapperBinder()))
        ServiceLocatorUtilities.bind(locator, *binders.toTypedArray())
        ServiceLocatorUtilities.enableImmediateScope(locator)
        return locator.getService(Locator::class.java)
    }

    fun bind(serviceConfig: MicroServiceConfig) : LocatorFactoryHK2Impl {
        this.binders.add(ServiceConfigBiner(serviceConfig))
        return this
    }

    fun bind(propertiesSource: PropertiesSource) : LocatorFactoryHK2Impl {
        this.binders.add(ConfigSourceBinder(propertiesSource))
        return this
    }

    fun bind(vararg binders: Binder) : LocatorFactoryHK2Impl {
        this.binders.addAll(binders)
        return this
    }

    private class ConfigSourceBinder(val propertiesSource: PropertiesSource) : AbstractBinder() {
        override fun configure() {
            bind(propertiesSource).to(PropertiesSource::class.java)
            bind(PropertiesInjectionResolver::class.java)
                    .to(object : TypeLiteral<InjectionResolver<Value>>() {})
                    .`in`(Singleton::class.java)
        }
    }

    private class ServiceConfigBiner(val serviceConfig: MicroServiceConfig) : AbstractBinder() {
        override fun configure() {
            bind(serviceConfig).to(MicroServiceConfig::class.java)
        }
    }

    private class ObjectMapperBinder : AbstractBinder() {
        override fun configure() {
            bindFactory(ObjectMapperProvider::class.java)
                    .to(ObjectMapper::class.java)
                    .`in`(Singleton::class.java)
        }
    }

    class LocatorBinder : AbstractBinder() {
        override fun configure() {
            bind(LocatorHK2Impl::class.java)
                    .to(Locator::class.java)
                    .`in`(Singleton::class.java)
        }
    }

}