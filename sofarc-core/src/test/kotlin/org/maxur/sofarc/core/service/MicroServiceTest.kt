package org.maxur.sofarc.core.service

import com.winterbe.expekt.expect
import com.winterbe.expekt.should
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.maxur.sofarc.core.service.hk2.MicroServiceBuilder
import java.net.URI

class MicroServiceTest: Spek({

    describe("a micro-service dsl") {

        val sut = MicroService

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

        it("should throw exception with defined config key for name without configuration") {
            try {
                val service: MicroService = sut.service()
                        .name(":name")
                        .build()
                expect(service).to.be.`null`
            } catch (e: IllegalStateException) {
                expect(e.message).to.be.equal("Service Configuration is not found. Key 'name' unresolved")
            }
        }

        it("return new micro-service with defined config key for name") {
            val builder =
                    sut.service(Binder())
                    .config()
                    .format("TEST")
                    .name(":name") as MicroServiceBuilder
            val service = builder.build()
            service.should.be.not.`null`
            service.name.should.be.equal("name")
        }
        
    }
})

class Binder : AbstractBinder() {

    override fun configure() {
        bind(PropertiesServiceTestImp::class.java).named("TEST").to(PropertiesService::class.java)
    }
}

object PropertiesServiceTestImp: PropertiesService {

    override fun asString(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asLong(key: String): Long? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asInteger(key: String): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asURI(key: String): URI? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun read(key: String, clazz: Class<*>): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
