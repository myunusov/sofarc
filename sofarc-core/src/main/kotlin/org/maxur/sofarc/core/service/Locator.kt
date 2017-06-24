package org.maxur.sofarc.core.service

import org.glassfish.hk2.utilities.Binder
import org.maxur.sofarc.core.service.properties.PropertiesSource

abstract class Locator {
    
    abstract fun bind(propertiesSource: PropertiesSource, vararg binders: Binder)

    abstract fun <T> service(clazz: Class<T>): T?

    abstract fun <T> service(clazz: Class<T>, name: String): T?

    abstract fun names(clazz: Class<*>): List<String>

    abstract fun property(key: String): String

}
