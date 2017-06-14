@file:Suppress("unused")

package org.maxur.sofarc.core.service.hocon

import com.fasterxml.jackson.databind.ObjectMapper
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.service.PropertiesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.util.function.Function
import javax.annotation.PostConstruct

/**
 * The type Properties service hocon.

 * @author Maxim Yunusov
 * *
 * @version 1.0
 * *
 * @since <pre>9/2/2015</pre>
 */
@Service
class PropertiesServiceHoconImpl : PropertiesService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PropertiesServiceHoconImpl::class.java)
    }

    private val objectMapper = ObjectMapper(HoconFactory())

    private var defaultConfig: Config? = null

    private var userConfig: Config? = null

    /**
     * init post construct
     */
    @PostConstruct
    fun init() {
        try {
            defaultConfig = ConfigFactory.load().getConfig("DEFAULTS")
        } catch(e: ConfigException.Missing) {
            log.error("The DEFAULT config not found. Add application.conf with DEFAULT section to classpass")
        }
        try {
            userConfig = ConfigFactory.load().getConfig("CUSTOMER")
        } catch(e: ConfigException.Missing) {
        }
    }

    override fun asURI(key: String): URI? {
        val string = asString(key)
        return when (string) {
            null -> null
            else -> URI.create(string)
        }
    }

    override fun asObject(key: String, clazz: Class<*>): Any? {
        try {
            val configObject: ConfigObject? = findValue(Function {it?.getObject(key)})
            if (configObject != null) {
                return objectMapper.readValue(configObject.render(), clazz)
            } else {
                return null
            }
        } catch (e: IOException) {
            log.error("Configuration parameter '$key' is not parsed.")
            log.error(e.message, e)
            return null
        }
    }

    override fun asString(key: String): String? {
        return getValue(key, Function { it?.getString(key)})
    }

    override fun asLong(key: String): Long? {
        return getValue(key, Function { it?.getLong(key)})
    }

    override fun asInteger(key: String): Int? {
        return getValue(key, Function { it?.getInt(key)})
    }
    
    private fun <T> getValue(key: String, transform: Function<Config?, T?>): T? {
        try {
            val value = findValue(transform)
            log.debug("Configuration parameter '$key' = '$value'")
            return value
        } catch (e: ConfigException.Missing) {
            log.error("Configuration parameter '$key' is not found.")
            throw e
        }
    }

    private fun <T> findValue(transform: Function<Config?, T?>): T? {
        try {
            if (userConfig != null) {
                return transform.apply(userConfig)
            }
        } catch (e: ConfigException.Missing) {
            log.debug(e.message, e)
        }
        return transform.apply(defaultConfig)
    }

}
