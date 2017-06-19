@file:Suppress("unused")

package org.maxur.sofarc.rest

import io.swagger.annotations.*
import org.jvnet.hk2.annotations.Service
import org.maxur.sofarc.core.annotation.Value
import org.maxur.sofarc.core.rest.RestResourceConfig
import javax.inject.Inject

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Service
@SwaggerDefinition(
        info = Info(
                title = "SOFTARC REST API",
                description = "This is a SoftArc Service REST API",
                version = "V1.0",

            //    termsOfService = "http://theweatherapi.io/terms.html",
                contact = Contact(
                        name = "Maxim Yunusov",
                        email = "myunusov@maxur.org",
                        url = "https://github.com/myunusov"
                ),
                license = License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        consumes = arrayOf("application/json", "application/xml"),
        produces = arrayOf("application/json", "application/xml"),
        schemes = arrayOf(SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS),
        tags = arrayOf(
            Tag(name = "Private", description = "Tag used to denote operations as private")
        )
       // externalDocs = ExternalDocs(value = "Meteorology", url = "http://theweatherapi.io/meteorology.html")
)
class RestServiceConfig @Inject constructor(
        @Value(key = "name") name: String
) : RestResourceConfig(name, RestServiceConfig::class.java.getPackage().name)