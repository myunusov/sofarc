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
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import org.maxur.sofarc.core.service.properties.PropertiesService
import javax.inject.Singleton

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
class LocatorFactoryHK2Impl(init: LocatorFactoryHK2Impl.() -> Unit) {

    val binders = ArrayList<Binder>()
    val bindersFunc = ArrayList<(Locator) -> Any>()

    val locator: ServiceLocator by lazy {
        ServiceLocatorUtilities.createAndPopulateServiceLocator()
    }

    fun make(): Locator {
        ServiceLocatorUtilities.enableImmediateScope(locator)
        ServiceLocatorUtilities.bind(locator, LocatorBinder())
        ServiceLocatorUtilities.bind(locator, ObjectMapperBinder())
        val result = locator.getService(Locator::class.java)
        bindersFunc.forEach {
            val service = it.invoke(result)
            ServiceLocatorUtilities.bind(locator, ServiceBinder(service))
            if (service is PropertiesService) {
                ServiceLocatorUtilities.bind(locator, PropertiesInjectionResolverBinder())
            }
        }
        ServiceLocatorUtilities.bind(locator, *binders.toTypedArray())
        return result
    }

    fun bind(vararg binders: Binder) : LocatorFactoryHK2Impl {
        this.binders.addAll(binders)
        return this
    }

    fun bind(func: (Locator) -> Any) {
        bindersFunc.add(func)
    }

    private class ServiceBinder(val service: Any) : AbstractBinder() {
        override fun configure() {
            bind(service).to(service.javaClass)
        }
    }

    private class PropertiesInjectionResolverBinder : AbstractBinder() {
        override fun configure() {
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
    class LocatorBinder : AbstractBinder() {
        override fun configure() {
            bind(LocatorHK2Impl::class.java)
                    .to(Locator::class.java)
                    .`in`(Singleton::class.java)
        }

    }

    init {
        init()
    }


}