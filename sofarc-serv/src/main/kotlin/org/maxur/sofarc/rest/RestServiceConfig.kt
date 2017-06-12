@file:Suppress("unused")

package org.maxur.sofarc.rest

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class RestServiceConfig : RestResourceConfig() {

    @Value("name")
    override lateinit var name: String

    override val restPackages: Array<String>
        get() = arrayOf(RestServiceConfig::class.java.getPackage().name)

}