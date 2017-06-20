package org.maxur.sofarc.core.service.jackson

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.IOException
import java.net.URI
import javax.ws.rs.core.Link


@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonDeserialize(using = LinkMixin.LinkDeserializer::class)
//@JsonSerialize(using = LinkMixin.LinkSerializer::class)
abstract class LinkMixin : Link() {

    @JsonProperty("href")
    override abstract fun getUri(): URI

    @JsonAnyGetter
    override abstract fun getParams(): Map<String, String>

    //see https://github.com/Nykredit/jackson-dataformat-hal

    /**
     *  {
            _links": {
                "self": { "href": "/orders" },
                "next": { "href": "/orders?page=2" }
            }
        }
     */
/*    class LinkSerializer : JsonSerializer<Link>() {

        override fun serialize(link: Link, gen: JsonGenerator, serializers: SerializerProvider) {
           return '"self": { "href": "/orders" }'
        }

    }*/
    
    class LinkDeserializer : JsonDeserializer<Link>() {

        @Throws(IOException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Link? {
            val value: TypeReference<Map<String, String>> = object : TypeReference<Map<String, String>>() {}
            val params: MutableMap<String, String> = p.readValueAs(value) ?: return null
            val uri = params.remove("href") ?: return null
            val builder = Link.fromUri(uri)
            params.forEach({ p1: String, p2: String -> builder.param(p1, p2) })
            return builder.build()
        }
    }


}

