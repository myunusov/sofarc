package org.maxur.sofarc.core.service

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paranamer.ParanamerModule
import org.glassfish.hk2.api.Factory


class ObjectMapperProvider : Factory<ObjectMapper> {

    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(KotlinModule())
                .registerModule(Jdk8Module())
                .registerModule(ParanamerModule())
                .registerModule(JavaTimeModule())         
    }

    override fun dispose(instance: ObjectMapper?) {
        // IGNORE
    }

    override fun provide(): ObjectMapper {
        return objectMapper;
    }


}