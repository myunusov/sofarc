package org.maxur.sofarc.core.rest

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.*
import org.maxur.sofarc.core.service.MicroService
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.Link
import javax.ws.rs.core.MediaType




/**
 * The type Application resource.
 *
 * @author Maxim Yunusov
 * @version 1.0
 * @since <pre>11/29/13</pre>
 */
@Path("/service")
@Api(value = "/service", description = "Endpoint for Service specific operations")
class ServiceResource @Inject constructor(val service: MicroService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Represent this service", response = ServiceView::class)
    @ApiResponses(value = *arrayOf(
            ApiResponse(code = 200, message = "Successful operation"),
            ApiResponse(code = 500, message = "Internal server error")
        )
    )
    fun service() : ServiceView {
        return ServiceView(service)
    }

    @PUT()
    @Path("/{state}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Change service state",
            notes = "Commands for stop or restart service"
    )
    @ApiResponses(value = *arrayOf(
            ApiResponse(code = 204, message = "Successful operation"),
            ApiResponse(code = 500, message = "Internal server error")
        )
    )
    fun state(
            @ApiParam(name = "state", value = "New service state", required = true, allowableValues="stop, restart")
            @PathParam("state") state: String
    ) {
        when (MicroService.State.from(state)) {
            MicroService.State.STOP -> service.stop()
            MicroService.State.RESTART -> service.restart()
        }
    }

}

@ApiModel(value="Service View", description="Service Presentation Model")
class ServiceView(service: MicroService) {

    @ApiModelProperty(value = "Service name")
    val name: String = service.name

    @Transient
    @JsonProperty("_links")
    val links: MutableList<Link> = ArrayList()
    
    init {
        links.add(Link.fromPath("/api/service").rel("self").build())
        links.add(Link.fromPath("/api/service/stop").rel("stop").build())
        links.add(Link.fromPath("/api/service/restart").rel("restart").build())
    }

}

