package org.maxur.sofarc.core.service

import org.glassfish.hk2.api.*
import org.glassfish.hk2.utilities.BuilderHelper
import org.maxur.sofarc.core.annotation.Value
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * Resolve configuration by ConfigParameter annotations.
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
class ConfigurationInjectionResolver @Inject constructor(
        val propertiesServices: IterableProvider<PropertiesService>,
        val locator: ServiceLocator
) : InjectionResolver<Value> {

    lateinit var propertiesService: PropertiesService

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigurationInjectionResolver::class.java)
    }

    @PostConstruct
    fun init() {
        //if (propertiesServices.size == 1) { TODO only first
            propertiesService = propertiesServices.get()
            val filter = BuilderHelper.createContractFilter(PropertiesService::class.java.name)
            val descriptors: List<ActiveDescriptor<*>> = locator.getDescriptors(filter)
            log.info("Configuration Properties Service is '${descriptors.get(0).name}'")
        //}
    }


    override fun resolve(injectee: Injectee, root: ServiceHandle<*>?): Any {
        val annotation = injectee.parent.getAnnotation(Value::class.java)
        val name = annotation.value
        val result = resolveByKey(name, injectee.requiredType)
        when (result) {
            null -> throw IllegalStateException("Property '$name' is not found")
            else -> return result
        }
    }

    private fun resolveByKey(name: String, type: Type): Any? {
        when (type.typeName) {
            "java.lang.String" -> return propertiesService.asString(name)
            "java.lang.Integer" -> return propertiesService.asInteger(name)
            "java.lang.Long" -> return propertiesService.asLong(name)
            "java.net.URI" -> return propertiesService.asURI(name)
            else -> {
                if (type is Class<*>) {
                    return propertiesService.asObject(name, type)
                }
                val msg = "Unsupported property type ${type.typeName}"
                log.error(msg)
                throw IllegalStateException(msg)
            }
        }
    }

    override fun isConstructorParameterIndicator(): Boolean {
        return false
    }

    override fun isMethodParameterIndicator(): Boolean {
        return false
    }
}
