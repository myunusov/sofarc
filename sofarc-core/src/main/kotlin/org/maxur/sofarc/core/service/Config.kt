package org.maxur.sofarc.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.annotation.Value
import javax.inject.Singleton

/**
 * Application Configurations
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>01.09.2015</pre>
 */
open class Config: AbstractBinder() {

    @SuppressWarnings("RedundantToBinding")
    @Override
    override fun configure() {
        bind<ConfigurationInjectionResolver>(ConfigurationInjectionResolver::class.java)
                .to(object : TypeLiteral<InjectionResolver<Value>>() {

                })
                .`in`(Singleton::class.java)

        bindFactory(ObjectMapperProvider::class.java).to(ObjectMapper::class.java).`in`(Singleton::class.java)
    }


}
