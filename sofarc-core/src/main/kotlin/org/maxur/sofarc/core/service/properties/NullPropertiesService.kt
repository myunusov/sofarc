package org.maxur.sofarc.core.service.properties

import java.net.URI

class NullPropertiesService : PropertiesService {
    override fun asString(key: String): String? = error(key)

    override fun asLong(key: String): Long? = error(key)

    override fun asInteger(key: String): Int? = error(key)

    override fun asURI(key: String): URI? = error(key)

    override fun <P> read(key: String, clazz: Class<P>): P? = error(key)

    private fun <T> error(key: String): T =
            throw IllegalStateException("Service Configuration is not found. Key '$key' unresolved")

}