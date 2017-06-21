package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.Injectee
import org.glassfish.hk2.api.InjectionResolver
import org.glassfish.hk2.api.ServiceHandle
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.PropertiesService
import org.maxur.sofarc.core.service.PropertiesServiceHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Type
import javax.inject.Inject


/**
 * Resolve configuration by ConfigParameter annotations.
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
class ConfigurationInjectionResolver @Inject constructor(holder: PropertiesServiceHolder) : InjectionResolver<Value> {

    val propertiesService: PropertiesService = holder.propertiesService

    companion object {
        val log: Logger = LoggerFactory.getLogger(ConfigurationInjectionResolver::class.java)
    }

    override fun resolve(injectee: Injectee, root: ServiceHandle<*>?): Any {
        val annotation = annotation(injectee)
        val name = annotation.key
        val result = resolveByKey(name, injectee.requiredType)
        when (result) {
            null -> throw IllegalStateException("Property '$name' is not found")
            else -> return result
        }
    }

    private fun annotation(injectee: Injectee): Value {
        val element = injectee.parent

        val isConstructor = element is Constructor<*>
        val isMethod = element is Method

        // if injectee is method or constructor, check its parameters
        if (isConstructor || isMethod) {
            val annotations: Array<Annotation>
            if (isMethod) {
                annotations = (element as Method).parameterAnnotations[injectee.position]
            } else {
                annotations = (element as Constructor<*>).parameterAnnotations[injectee.position]
            }

            for (annotation in annotations) {
                if (annotation is Value) {
                    return annotation
                }
            }
        }

        // check injectee itself (method, constructor or field)
        if (element.isAnnotationPresent(Value::class.java)) {
            return element.getAnnotation(Value::class.java)
        }

        // check class which contains injectee
        val clazz = injectee.injecteeClass
        return clazz.getAnnotation(Value::class.java)
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
        return true
    }

    override fun isMethodParameterIndicator(): Boolean {
        return false
    }
}
