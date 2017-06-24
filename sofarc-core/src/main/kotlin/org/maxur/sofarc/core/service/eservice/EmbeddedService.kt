package org.maxur.sofarc.core.service.eservice

import org.maxur.sofarc.core.service.Locator

/**
 * This class represents Embedded to micro-service Service
 * with start and stop functions
 */
abstract class EmbeddedService<P: Any>(val strategy: PropertiesStrategy) {

    constructor() : this(NonePropertiesStrategy())
    constructor(properties: P): this(DirectPropertiesStrategy<P>(properties))
    constructor(locator: Locator, propertyKey: String): this(PropertiesByKeyStrategy<P>(locator, propertyKey))

    inline fun <reified R: P> properties(): R? {
        when(strategy) {
            is NonePropertiesStrategy -> return null
            is DirectPropertiesStrategy<*> -> return strategy.properties as R?
            is PropertiesByKeyStrategy<*> -> return strategy.locator.properties(strategy.key, R::class.java)
            else -> return null
        }
    }

    /**
     * Start Web server.
     */
    abstract fun start()

    /**
     * Stop Web server.
     */
    abstract fun stop()

    interface PropertiesStrategy

    data class PropertiesByKeyStrategy<P>(val locator: Locator, val key: String): PropertiesStrategy

    data class DirectPropertiesStrategy<P>(val properties: P): PropertiesStrategy

    class NonePropertiesStrategy: PropertiesStrategy

}