@file:Suppress("unused")

package org.maxur.sofarc.params

import com.fasterxml.jackson.databind.SerializationFeature
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import org.maxur.sofarc.core.service.embedded.properties.WebAppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * All Configuration Parameters
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class ConfigParams @Inject constructor(
        @Value(key = "webapp") val webapp: WebAppProperties,
        @Value(key = "name")   val name: String
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigParams::class.java)
    }
    
    fun log() {
        log.info("\n--- Configuration Parameters ---\n")
        log.info("$this")
        log.info("\n---------------------------------\n")
    }

    override fun toString(): String {
        val mapper = ObjectMapperProvider().provide()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(this)
    }

}