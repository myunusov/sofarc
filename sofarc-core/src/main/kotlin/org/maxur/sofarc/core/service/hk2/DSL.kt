package org.maxur.sofarc.core.service.hk2

import org.maxur.sofarc.core.MicroService


object DSL {

    fun service(init: MicroServiceBuilder.() -> Unit): MicroService =
            MicroServiceBuilder(init).build()
}