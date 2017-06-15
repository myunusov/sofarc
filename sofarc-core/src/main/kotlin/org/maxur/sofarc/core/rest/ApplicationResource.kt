package org.maxur.sofarc.core.rest

import org.maxur.sofarc.core.service.MicroService
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


/**
 * The type Application resource.
 *
 * @author Maxim Yunusov
 * @version 1.0
 * @since <pre>11/29/13</pre>
 */
@Path("/application")
open class ApplicationResource @Inject constructor(val service: MicroService) {

    @PUT()
    @Path("/{state}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun state(@PathParam("state") state: String) {
        when (MicroService.State.from(state)) {
            MicroService.State.STOP -> service.stop()
            MicroService.State.RESTART -> service.restart()
        }
    }

}

