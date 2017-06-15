package org.maxur.sofarc.params

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.grizzly.WebAppConfig
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
        @Value(key = "webapp") val webapp: WebAppConfig,
        @Value(key = "name")   val name: String
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigParams::class.java)
    }
    
    fun log() {
        log.info("--- Configuration Parameters ---")
        log.info("name   = $name")
        log.info("webapp = $webapp")
        log.info("--- --- --- --- --- --- --- --- ---")
    }

}