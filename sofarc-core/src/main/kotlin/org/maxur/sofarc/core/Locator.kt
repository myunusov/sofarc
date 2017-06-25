package org.maxur.sofarc.core

interface Locator {

    fun <T> service(clazz: Class<T>): T?

    fun <T> service(clazz: Class<T>, name: String): T?

    fun names(clazz: Class<*>): List<String>

    fun property(key: String): String

    fun <R> properties(key: String, clazz: Class<R>): R?

    fun <T> implementation(): T

}
