package org.maxur.sofarc.core.rest

import org.maxur.sofarc.core.service.ApplicationState
import org.maxur.sofarc.core.service.MicroService
import java.util.*
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.concurrent.schedule


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
        when (ApplicationState.from(state)) {
            ApplicationState.STOP -> postpone({ stop() })
            ApplicationState.RESTART -> postpone({ restart() })
        }
    }

    private fun restart() {
        service.onStop()
        service.onStart()
    }

    private fun stop() {
        service.onStop()
    }
    
    fun postpone(func: Function<Unit>) {
        Timer("schedule", true).schedule(100) {
            (func as () -> Unit).invoke()
        }
    }


}

