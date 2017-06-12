package org.maxur.sofarc.core.service

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.hk2.utilities.Binder
import org.glassfish.hk2.utilities.ServiceLocatorUtilities

object IoC {

    fun newLocator(vararg binders: Binder): ServiceLocator {
        val serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        ServiceLocatorUtilities.bind(serviceLocator, Config(), *binders)
        return serviceLocator.getService<ServiceLocator>(ServiceLocator::class.java)
    }

}