@file:Suppress("unused")

package org.maxur.sofarc.core.service.hocon

import com.fasterxml.jackson.databind.ObjectMapper
import com.jasonclawson.jackson.dataformat.hocon.HoconFactory
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.service.ConfigSource
import org.maxur.sofarc.core.service.PropertiesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.util.function.Function
import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * The type Properties service hocon.

 * @author Maxim Yunusov
 * *
 * @version 1.0
 * *
 * @since <pre>9/2/2015</pre>
 */
@Service(name = "Hocon")
class PropertiesServiceHoconImpl @Inject constructor(val source: ConfigSource) : PropertiesService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PropertiesServiceHoconImpl::class.java)
    }

    private lateinit var strategy: PropertiesService

    @PostConstruct
    fun init() {
        try {
            strategy = SuccessStrategy(ConfigFactory.load().getConfig(source.rootKey))
        } catch(e: ConfigException.Missing) {
            strategy = FailStrategy()
            log.warn("The '${source.rootKey}' config not found. " +
                    "Add application.conf with '${source.rootKey}' section to classpath")
        }
    }

    override fun asString(key: String): String? = strategy.asString(key)
    override fun asLong(key: String): Long?  = strategy.asLong(key)
    override fun asInteger(key: String): Int? = strategy.asInteger(key)
    override fun asURI(key: String): URI?  = strategy.asURI(key)
    override fun read(key: String, clazz: Class<*>): Any?  = strategy.read(key, clazz)
}

class SuccessStrategy(val config: Config) : PropertiesService {

    private val objectMapper = ObjectMapper(HoconFactory())

    override fun read(key: String, clazz: Class<*>): Any? {
        when (clazz.typeName) {
            "java.lang.String" -> return asString(key)
            "java.lang.Integer" -> return asInteger(key)
            "java.lang.Long" -> return asLong(key)
            "java.net.URI" -> return asURI(key)
            else -> return asObject(key, clazz)
        }
    }

    override fun asURI(key: String): URI? {
        val string = asString(key)
        return when (string) {
            null -> null
            else -> URI.create(string)
        }
    }

    private fun asObject(key: String, clazz: Class<*>): Any? {
        try {
            val configObject: ConfigObject? = findValue(Function { it?.getObject(key) })
            if (configObject != null) {
                return objectMapper.readValue(configObject.render(), clazz)
            } else {
                return null
            }
        } catch (e: IOException) {
            PropertiesServiceHoconImpl.log.error("Configuration parameter '$key' is not parsed.")
            PropertiesServiceHoconImpl.log.error(e.message, e)
            return null
        }
    }

    override fun asString(key: String): String? {
        return getValue(key, Function { it?.getString(key) })
    }

    override fun asLong(key: String): Long? {
        return getValue(key, Function { it?.getLong(key) })
    }

    override fun asInteger(key: String): Int? {
        return getValue(key, Function { it?.getInt(key) })
    }

    private fun <T> getValue(key: String, transform: Function<Config?, T?>): T? {
        try {
            val value = findValue(transform)
            PropertiesServiceHoconImpl.log.debug("Configuration parameter '$key' = '$value'")
            return value
        } catch (e: ConfigException.Missing) {
            PropertiesServiceHoconImpl.log.error("Configuration parameter '$key' is not found.")
            throw e
        }
    }

    private fun <T> findValue(transform: Function<Config?, T?>): T? {
        return transform.apply(config)
    }

}

class FailStrategy : PropertiesService {

    override fun asString(key: String): String? = error(key)

    override fun asLong(key: String): Long? = error(key)

    override fun asInteger(key: String): Int? = error(key)

    override fun asURI(key: String): URI? = error(key)

    override fun read(key: String, clazz: Class<*>): Any? = error(key)

    private fun <T> error(key: String): T =
            throw IllegalStateException("Service Configuration is not found. Key '$key' unresolved")

}

