package org.maxur.sofarc.core.service

class ConfigSource(val format: String, val rootKey: String, val structure: Class<Any>) {

    companion object Builder {

        var format: String = "Hocon"

        var rootKey: String = "DEFAULT"

        fun fromClasspath(): Builder = this
        
        fun format(value: String): Builder {
            format = value
            return this
        }

        fun rootKey(value: String): Builder {
            rootKey = value
            return this
        }

        fun writeTo(structure: Class<*>): ConfigSource {
            @Suppress("UNCHECKED_CAST")
            return ConfigSource(format, rootKey, structure as Class<Any>)
        }
    }

}



