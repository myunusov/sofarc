package org.maxur.sofarc.core.domain

interface Factory<T> {
    fun get(): T
}



