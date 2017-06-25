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
abstract class EmbeddedServiceFactory<P: Any> {

    inline fun <reified R: P> properties(key: String, locator: Locator): R? {
        return locator.properties(key, R::class.java)
    }

    @Inject
    @Self
    private var descriptor: ActiveDescriptor<*>? = null

    lateinit var name: String

    @PostConstruct
    fun init() {
       name = descriptor?.name ?: "Undefined"
    }

    abstract fun make(cfg: ServiceConfig.LookupDescriptor): EmbeddedService?

}