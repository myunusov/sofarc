package org.maxur.sofarc.core.service

import org.glassfish.hk2.utilities.Binder

abstract class Locator {
    
    abstract fun bind(vararg binders: Binder)

    abstract fun bind(configSource: ConfigSource)

    abstract fun <T> service(clazz: Class<T>): T?

    abstract fun <T> service(clazz: Class<T>, name: String): T?

    abstract fun names(clazz: Class<*>): List<String>

    abstract fun value(key: String): String

}
