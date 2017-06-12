package org.maxur.sofarc.core.rest

import org.glassfish.jersey.ServiceLocatorProvider
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.ServerProperties
import org.jvnet.hk2.annotations.Contract
import javax.annotation.PostConstruct
import javax.ws.rs.core.Feature
import javax.ws.rs.core.FeatureContext

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
abstract class RestResourceConfig : ResourceConfig() {

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
        //register(RuntimeExceptionHandler::class.java)
        //register(ValidationExceptionHandler::class.java)
        register(ServiceLocatorFeature())
        //register(ServiceEventListener("api/v1.0/reports"))
        //register(DiagnosticResource::class.java)
        //register(MultiPartFeature::class.java)
    }

    protected abstract val restPackages: Array<String>

    protected abstract val name: String

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