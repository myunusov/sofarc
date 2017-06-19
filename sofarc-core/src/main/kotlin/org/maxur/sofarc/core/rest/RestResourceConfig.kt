package org.maxur.sofarc.core.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import io.swagger.config.SwaggerConfig
import io.swagger.jaxrs.config.DefaultJaxrsScanner
import io.swagger.jaxrs.config.SwaggerContextService
import io.swagger.models.Info
import io.swagger.models.Scheme
import io.swagger.models.Swagger
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

        val scanner = DefaultJaxrsScanner()

        SwaggerContextService()
                .withSwaggerConfig(object : SwaggerConfig {
                    override fun configure(swagger: Swagger): Swagger {
                        val info = Info()
                        info.setTitle("Rest Resource")
                        info.setVersion("1.0")
                        swagger.info = info
                        swagger.basePath = "/" + webConfig.apiPath
                        swagger.host = "${webConfig.url.host}:${webConfig.url.port}"
                        swagger.schemes = listOf(Scheme.HTTP)
                        return swagger
                    }

                    override fun getFilterClass(): String? {
                        return null
                    }
                })
                .withScanner(scanner)
                .initConfig()
                .initScanner()

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