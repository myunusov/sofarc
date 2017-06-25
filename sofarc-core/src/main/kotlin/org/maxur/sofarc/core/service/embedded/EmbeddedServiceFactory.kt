package org.maxur.sofarc.core.service.embedded

import org.glassfish.hk2.api.ActiveDescriptor
import org.glassfish.hk2.api.Self
import org.jvnet.hk2.annotations.Contract
import org.maxur.sofarc.core.Locator
import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>24.06.2017</pre>
 */
@Contract
abstract class EmbeddedServiceFactory<PropertiesType: Any> {

    @Inject
    @Self
    private var descriptor: ActiveDescriptor<*>? = null

    @Inject
    lateinit var locator: Locator

    lateinit var name: String

    @PostConstruct
    fun init() {
       name = descriptor?.name ?: "Undefined"
    }

    inline fun <reified R : PropertiesType> properties(cfg: ServiceDescriptor<R>): R?
            = cfg.properties(locator, R::class.java)

    abstract fun make(cfg: ServiceDescriptor<PropertiesType>): EmbeddedService?

}