package org.maxur.sofarc.core.rest

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import io.swagger.jaxrs.config.BeanConfig
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.ServerProperties
import org.jvnet.hk2.annotations.Contract
import org.maxur.sofarc.core.embedded.properties.WebAppProperties
import org.maxur.sofarc.core.service.jackson.ObjectMapperProvider
import javax.annotation.PostConstruct
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext


/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
abstract class RestResourceConfig(
        val name: String,
        val properties: WebAppProperties,
        vararg val restPackages: String
) : ResourceConfig() {

    @PostConstruct
    fun init() {
        applicationName = name

        val list: MutableList<String> = ArrayList()
        list.addAll(restPackages)
        list.add(RestResourceConfig::class.java.`package`.name)

        if (properties.withSwaggerUi)
            list.add(io.swagger.jaxrs.listing.ApiListingResource::class.java.`package`.name)

        list.forEach {
            packages(it)
        }

        if (properties.withSwaggerUi)
            initSwagger(list)

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true)
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true)

        register(JacksonFeature::class.java)
        register(RuntimeExceptionHandler::class.java)
        register(ServiceLocatorFeature())
        register(ServiceEventListener("/"))
        register(MultiPartFeature::class.java)
        
        val provider = JacksonJaxbJsonProvider()
        provider.setMapper(ObjectMapperProvider().provide())
        register(provider)
    }

    private fun initSwagger(packages: MutableList<String>) {
        val config = BeanConfig()
        config.basePath = "/" + this.properties.rest.path
        config.host = "${properties.url.host}:${properties.url.port}"
        config.resourcePackage = packages.joinToString(",")
        config.scan = true
    }

    /**
     * service locator feature
     */
    private class ServiceLocatorFeature : Feature {

        override fun configure(context: FeatureContext): Boolean {
            ServiceLocatorProvider.getServiceLocator(context)
            return true
        }
    }

}