package org.maxur.sofarc.core.service.hk2

import com.fasterxml.jackson.databind.ObjectMapper
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import javax.inject.Singleton

/**
 * Application Configurations
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>01.09.2015</pre>
 */
open class Config(val configSource: ConfigSource) : AbstractBinder() {

    @SuppressWarnings("RedundantToBinding")
    @Override
    override fun configure() {
        bind(configSource).to(ConfigSource::class.java)

        bind<ConfigurationInjectionResolver>(ConfigurationInjectionResolver::class.java)
                .to(object : TypeLiteral<InjectionResolver<Value>>() {

                })
                .`in`(Singleton::class.java)

        bindFactory(ObjectMapperProvider::class.java).to(ObjectMapper::class.java).`in`(Singleton::class.java)
    }


}
