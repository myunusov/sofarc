package org.maxur.sofarc.core.service

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.jetbrains.spek.api.Spek
import org.maxur.sofarc.core.domain.Holder
import org.maxur.sofarc.core.embedded.EmbeddedService
import org.maxur.sofarc.core.embedded.EmbeddedServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesService
import org.maxur.sofarc.core.service.properties.PropertiesServiceFactory
import org.maxur.sofarc.core.service.properties.PropertiesSource

class MicroServiceSpec : Spek({


    /*          MicroService.javaRestService()
                      .name(":name")
                      .beforeStart(this::beforeStart)
                      .afterStop(this::afterStop)
                      .onError(this::onError)
                      .start()*/


/*        service()
                .name(":name")
                .web()
                .beforeStart(this::beforeStart)
                .afterStop(this::afterStop)
                .onError(this::onError)
                .start()*/

/*        service()
                .name(":name")
                .properties().fromClasspath().rootKey("DEFAULTS")
                .embed("Grizzly").propertiesKey(":webapp")
                .beforeStart(this::beforeStart)
                .afterStop(this::afterStop)
                .onError(this::onError)
                .start()*/

/*    describe("a micro-service dsl Builder") {
        val sut = MicroService

        on("Build microservice without properties source") {

            it("should return new micro-service without embedded services") {
                val service = (sut.javaService() as JavaMicroServiceBuilder).build()
                service.should.be.not.`null`
            }

            it("should return new micro-service with defined name") {
                val service: MicroService = (sut.javaService() as JavaMicroServiceBuilder)
                        .name("TEST1")
                        .build()
                service.should.be.not.`null`
                service.name.should.be.equal("TEST1")
            }

            it("should throw exception with any property key") {
                try {
                    val service: MicroService = (sut.javaService() as JavaMicroServiceBuilder)
                            .name(":name")
                            .build()
                    expect(service).to.be.`null`
                } catch (e: IllegalStateException) {
                    expect(e.message).to.be.equal("Service Configuration is not found. Key 'name' unresolved")
                }
            }

        }

        on("Build microservice with properties source") {


            it("should return new micro-service with name from properties") {
                val builder =
                        sut.javaService(Binder())
                                .properties().format("config")
                                .name(":name") as JavaMicroServiceBuilder
                val service = builder.build()
                service.should.be.not.`null`
                service.name.should.be.equal("name")
            }

            it("should return new micro-service with embedded service") {
                val builder =
                        sut.javaService(Binder())
                                .embed("service1")
                                .name("TEST2") as JavaMicroServiceBuilder

                val service = builder.build()
                service.should.be.not.`null`
            }

            it("should return new micro-service with few embedded services") {
                val builder =
                        sut.javaService(Binder())
                                .embed("service1")
                                .embed("service2")
                                .name("TEST2") as JavaMicroServiceBuilder

                val service = builder.build()
                service.should.be.not.`null`

                service.service

                service.start()
                service.stop()
            }

            it("should start new micro-service with few embedded services") {
                val service1 = mock<EmbeddedService> {}
                val service2 = mock<EmbeddedService> {}
                val builder =  (sut.javaService(Binder()) as JavaMicroServiceBuilder)
                                .embed(service1)
                                .embed(service2)
                                .name("TEST2")

                val service = builder.build()
                service.should.be.not.`null`

                service.service

                service.start()
                verify(service1, times(1)).start()
                verify(service2, times(1)).start()

            }

        }

    }*/
})


class Binder : AbstractBinder() {

    class TestPropertiesServiceFactory : PropertiesServiceFactory() {
        override fun make(source: PropertiesSource): PropertiesService? = mock {
            on { asString("name") } doReturn "name"
        }
    }

    class TestServiceFactory : EmbeddedServiceFactory() {
        val embeddedService = mock<EmbeddedService> {}
        override fun make(properties: Holder<Any?>): EmbeddedService? = embeddedService
    }

    override fun configure() {
        bind(TestPropertiesServiceFactory::class.java).named("config").to(PropertiesServiceFactory::class.java)
        bind(TestServiceFactory::class.java).named("service1").to(EmbeddedServiceFactory::class.java)
        bind(TestServiceFactory::class.java).named("service2").to(EmbeddedServiceFactory::class.java)
    }

}

