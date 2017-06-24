package org.maxur.sofarc.core.service.eservice

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
data class ServiceConfig(val descriptor: Descriptor) {

    constructor(
            type: String,
            propertyKey: String?,
            properties: Any?, clazz:
            Class<*> = EmbeddedServiceFactory::class.java
    ) : this(LookupDescriptor(type, propertyKey, properties, clazz))

    constructor(service: EmbeddedService<Any>) : this(DirectDescriptor(service))

    interface Descriptor {}

    data class LookupDescriptor(
            val type: String,
            val propertyKey: String?,
            val properties: Any?,
            val clazz: Class<*> = EmbeddedServiceFactory::class.java
    ): Descriptor

    data class DirectDescriptor(val service: EmbeddedService<Any>): Descriptor
}

