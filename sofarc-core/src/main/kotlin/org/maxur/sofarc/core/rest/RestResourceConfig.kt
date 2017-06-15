package org.maxur.sofarc.core.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.ServerProperties
import org.jvnet.hk2.annotations.Contract
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
abstract class RestResourceConfig(val name: String, vararg val restPackages: String) : ResourceConfig() {

    @Inject
    lateinit var mapper: ObjectMapper;

    @PostConstruct
    fun init() {
        applicationName = name

        restPackages.forEach {
            packages(it)
        }
        packages(RestResourceConfig::class.java.getPackage().name)
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