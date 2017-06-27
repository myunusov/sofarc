package org.maxur.sofarc.core.embedded

import org.maxur.sofarc.core.Locator
import org.maxur.sofarc.core.domain.Holder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
class ServiceConfig {

    private val service: Holder<EmbeddedService?>
    val properties: Holder<Any?>

    constructor(service: EmbeddedService) {
        this.properties = Holder.wrap<Any?>(null)
        this.service = Holder.wrap<EmbeddedService?>(service)
    }

    constructor(type: String, properties: Holder<Any?>) {
        this.properties = properties
        this.service = Holder.get{ locator -> ServiceFactory(type, properties).make(locator) }
    }

    fun service(locator: Locator): EmbeddedService? = service.get<EmbeddedService>(locator)

}


private class ServiceFactory(val name: String, private val holder: Holder<Any?>) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ServiceFactory::class.java)
    }

    fun make(locator: Locator): EmbeddedService? {
        val clazz = EmbeddedServiceFactory::class.java
        val factory = locator.service(clazz, name) ?: return serviceNotFoundError(locator)
        return factory.make(holder)
                .let { success(factory, it) } ?:
                serviceNotCreatedError()
    }

    private fun serviceNotFoundError(locator: Locator): EmbeddedService? {
        val list = locator.names(EmbeddedServiceFactory::class.java)
        throw IllegalStateException(
                "Service '$name' is not supported. Try one from this list: $list"
        )
    }

    private fun success(factory: EmbeddedServiceFactory, result: EmbeddedService?): EmbeddedService? {
        log.info("Service '${factory.name}' is configured\n")
        return result
    }

    private fun serviceNotCreatedError(): Nothing? {
        log.info("Service '$name' is not configured\n")
        return null
    }

}
