package org.maxur.sofarc.core.service

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.winterbe.expekt.expect
import com.winterbe.expekt.should
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.maxur.sofarc.core.service.hk2.MicroServiceBuilder
import org.maxur.sofarc.core.service.properties.PropertiesService
import org.maxur.sofarc.core.service.properties.PropertiesServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesSource

class MicroServiceSpec : Spek({

    describe("a micro-service dsl Builder") {
        val sut = MicroService

        on ("Build microservice without properties source") {

            it("should return new micro-service without embedded services") {
                val service = sut.service()
                        .build()
                service.should.be.not.`null`
            }

            it("should return new micro-service with defined name") {
                val service: MicroService = sut.service()
                        .name("TEST1")
                        .build()
                service.should.be.not.`null`
                service.name.should.be.equal("TEST1")
            }

            it("should throw exception with any property key") {
                try {
                    val service: MicroService = sut.service()
                            .name(":name")
                            .build()
                    expect(service).to.be.`null`
                } catch (e: IllegalStateException) {
                    expect(e.message).to.be.equal("Service Configuration is not found. Key 'name' unresolved")
                }
            }

        }

        on ("Build microservice with properties source") {
            val binder = Binder()

            it("return new micro-service with name from properties") {
                val builder =
                        sut.service(binder)
                                .config().format("config")
                                .name(":name") as MicroServiceBuilder
                val service = builder.build()
                service.should.be.not.`null`
                service.name.should.be.equal("name")
            }

            it("return and start new micro-service with embedded service") {
                val builder =
                        sut.service(binder)
                                .embed("service")
                                .name("TEST2") as MicroServiceBuilder

                val service = builder.build()
                service.should.be.not.`null`
                service.start()
                verify(binder.embeddedService, times(1)).start()
                verify(binder.embeddedService, times(0)).stop()
            }

        }

    }
})

class Binder : AbstractBinder() {

    class TestPropertiesServiceFactory : PropertiesServiceFactory() {
        override fun make(source: PropertiesSource): PropertiesService? = mock {
            on { asString("name") } doReturn "name"
        }
    }

    val embeddedService = mock<EmbeddedService> {
    }

    override fun configure() {
        bind(TestPropertiesServiceFactory::class.java).named("config").to(PropertiesServiceFactory::class.java)
        bind(embeddedService).named("service").to(EmbeddedService::class.java)
    }

}

