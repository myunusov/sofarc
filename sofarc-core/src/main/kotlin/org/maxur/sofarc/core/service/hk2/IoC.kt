package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.maxur.sofarc.core.service.Config
import org.maxur.sofarc.core.service.MicroService
import org.slf4j.LoggerFactory

object IoC {

    private fun log() = LoggerFactory.getLogger(IoC::class.java)

    fun application(vararg binders: Binder): MicroService {
        try {
            return newLocator(*binders).getService<MicroService>(MicroService::class.java)
        } catch (e:Exception) {
            log().error("Application is not configured")
            throw IllegalStateException("Application is not configured", e)
        }
    }

    fun newLocator(vararg binders: Binder): ServiceLocator {
        val serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        ServiceLocatorUtilities.bind(serviceLocator, Config(), *binders)
        return serviceLocator.getService<ServiceLocator>(ServiceLocator::class.java)
    }

}