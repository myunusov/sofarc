package org.maxur.sofarc.core.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sun.security.timestamp.TSResponse.BAD_REQUEST
import javax.ws.rs.core.GenericEntity
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper

/**
 * The type Runtime exception handler.

 * @author myunusov
 * *
 * @version 1.0
 * *
 * @since <pre>11.09.2015</pre>
 */
class RuntimeExceptionHandler : ExceptionMapper<RuntimeException> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(RuntimeExceptionHandler::class.java)
    }

    override fun toResponse(exception: RuntimeException): Response {
        if (exception is IllegalArgumentException) {
            log.warn(exception.message)
            log.debug(exception.message, exception)
            return status(BAD_REQUEST)
                    .type(APPLICATION_JSON)
                    .entity(makeUserErrorEntity(exception))
                    .build()
        } else {
            log.error(exception.message, exception)
            return status(Status.INTERNAL_SERVER_ERROR)
                    .type(APPLICATION_JSON)
                    .entity(makeSystemErrorEntity(exception))
                    .build()
        }
    }

    private fun makeSystemErrorEntity(exception: RuntimeException): GenericEntity<List<Incident>> {
        return object : GenericEntity<List<Incident>>(
                Incident.incidents("System error", exception.message ?: "Unknown")
        ) {
        }
    }

    private fun makeUserErrorEntity(exception: RuntimeException): GenericEntity<List<Incident>> {
        return object : GenericEntity<List<Incident>>(
                Incident.incidents("Bad parameters", exception.message ?: "Unknown")
        ) {

        }
    }


}



