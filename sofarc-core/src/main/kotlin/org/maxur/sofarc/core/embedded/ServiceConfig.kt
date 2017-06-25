package org.maxur.sofarc.core.embedded

import org.maxur.sofarc.core.Locator

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
interface ServiceConfig

class ServiceDescriptor<PropertiesType>(
        val type: String,
        val propertyKey: String?,
        val properties: PropertiesType?
) : ServiceConfig {

    fun <R : PropertiesType> properties(locator: Locator, clazz: Class<R>): R? {
        @Suppress("UNCHECKED_CAST")
        return when {
            properties != null -> properties as R
            propertyKey != null -> {
                locator.properties(propertyKey, clazz)
            }
            else -> null
        }
    }

}

data class ServiceProxy(val service: EmbeddedService) : ServiceConfig
