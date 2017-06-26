package org.maxur.sofarc.core.embedded

import org.maxur.sofarc.core.Locator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
abstract class ServiceConfig {
    
    companion object {
        fun make(service: EmbeddedService): ServiceConfig = ServiceProxy(service)
        fun make(type: String, properties: Any): ServiceConfig
                = ServiceDescriptor(type, PropertyProxy(properties))
        fun make(type: String, propertyKey: String): ServiceConfig
                = ServiceDescriptor(type, PropertyDescriptor(propertyKey))
        val log: Logger = LoggerFactory.getLogger(ServiceConfig::class.java)
    }

    abstract val isDescriptor: Boolean

    abstract fun  service(locator: Locator): EmbeddedService?

    abstract fun <PropertiesType> properties(locator: Locator, clazz: Class<PropertiesType>): PropertiesType?
}


private data class ServiceProxy(val service: EmbeddedService) : ServiceConfig() {

    override val isDescriptor: Boolean = false

    override fun <PropertiesType> properties(locator: Locator, clazz: Class<PropertiesType>): PropertiesType? = null

    override fun service(locator: Locator): EmbeddedService = service
}


private class ServiceDescriptor(
        val type: String,
        val properties: PropertyHolder
) : ServiceConfig() {

    override val isDescriptor: Boolean = true

    override fun service(locator: Locator): EmbeddedService? {
        val clazz = EmbeddedServiceFactory::class.java
        val factory = locator.service(clazz, type) ?: return serviceNotFoundError(locator)
        return factory.make(this)
                .let { success(factory, it) } ?:
                serviceNotCreatedError()
    }

    private fun success(factory: EmbeddedServiceFactory<*>, result: EmbeddedService?): EmbeddedService? {
        log.info("Service '${factory.name}' is configured\n")
        return result
    }

    private fun serviceNotCreatedError(): Nothing? {
        log.info("Service '$type' is not configured\n")
        return null
    }

    private fun serviceNotFoundError(locator: Locator): EmbeddedService? {
        val list = locator.names(EmbeddedServiceFactory::class.java)
        throw IllegalStateException(
                "Service '$type' is not supported. Try one from this list: $list"
        )
    }

    override fun <PropertiesType> properties(locator: Locator, clazz: Class<PropertiesType>): PropertiesType?
            = properties.get(locator, clazz)

}

interface PropertyHolder {
    fun <PropertiesType> get(locator: Locator, clazz: Class<PropertiesType>): PropertiesType?
}

private class PropertyProxy(val properties: Any) : PropertyHolder {
    @Suppress("UNCHECKED_CAST")
    override fun <PropertiesType> get(locator: Locator, clazz: Class<PropertiesType>): PropertiesType? =
            properties as  PropertiesType
}

private class PropertyDescriptor(val key: String) : PropertyHolder {
    override fun <PropertiesType> get(locator: Locator, clazz: Class<PropertiesType>): PropertiesType?
            = locator.properties(key, clazz)

}

