package org.maxur.sofarc.core.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import io.swagger.jaxrs.config.BeanConfig
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.ServerProperties
import org.jvnet.hk2.annotations.Contract
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.service.grizzly.config.WebAppConfig
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext


/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
abstract class RestResourceConfig(val name: String,  vararg val restPackages: String) : ResourceConfig() {

    @Inject
    lateinit var mapper: ObjectMapper

    @Value(key = "webapp")
    lateinit var webConfig: WebAppConfig

    @PostConstruct
    fun init() {
        applicationName = name

        val list: MutableList<String> = ArrayList()
        list.addAll(restPackages)
        list.add(io.swagger.jaxrs.listing.ApiListingResource::class.java.`package`.name)
        list.add(RestResourceConfig::class.java.`package`.name)

        list.forEach {
            packages(it)
        }

        initSwagger(list)

        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true)
        property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true)

        register(JacksonFeature::class.java)

        register(RuntimeExceptionHandler::class.java)

        register(ServiceLocatorFeature())
        register(ServiceEventListener("/"))
        register(MultiPartFeature::class.java)


        
        val provider = JacksonJaxbJsonProvider()
        provider.setMapper(mapper)
        register(provider)
    }

    private fun initSwagger(packages: MutableList<String>) {
        val config = BeanConfig()
        config.basePath = "/" + webConfig.apiPath
        config.host = "${webConfig.url.host}:${webConfig.url.port}"
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