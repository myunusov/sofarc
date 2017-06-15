@file:Suppress("unused")

package org.maxur.sofarc.rest

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class RestServiceConfig @Inject constructor(
        @Value(key = "name") name: String
) : RestResourceConfig(name, RestServiceConfig::class.java.getPackage().name)