package org.maxur.sofarc.params

import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * All Configuration Parameters
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
class ConfigParams {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigParams::class.java)
    }

    @Value("webapp")
    lateinit var webapp: WebApp

    @Value("name")
    lateinit var name: String

    fun log() {
        log.info("--- Configuration Parameters ---")
        log.info("name   = $name")
        log.info("webapp = $webapp")
        log.info("--- --- --- --- --- --- --- --- ---")
    }

}