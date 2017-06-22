package org.maxur.sofarc.core.rest

import dk.nykredit.jackson.dataformat.hal.HALLink
import dk.nykredit.jackson.dataformat.hal.annotation.Link
import dk.nykredit.jackson.dataformat.hal.annotation.Resource
import io.swagger.annotations.*
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.maxur.sofarc.core.service.MicroService
import java.net.URI
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
@Path("/service")
@Api(value = "/service", description = "Endpoint for Service specific operations")
@RequiresPermissions("protected:read")
class ServiceResource @Inject constructor(val service: MicroService) {

    @GET
    @Produces("application/hal+json")
    @ApiOperation(value = "Represent this service",
            response = ServiceView::class, produces = "application/hal+json")
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
    @RequiresPermissions("protected:write")
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

@Suppress("unused")
@Resource
@ApiModel(value="Service View", description="Service Presentation Model")
class ServiceView(service: MicroService) {

    @ApiModelProperty(value = "Service name")
    val name: String = service.name

    @Link
    var self: HALLink = HALLink.Builder(URI("service")).build()

    @Link("stop")
    var stop: HALLink = HALLink.Builder(URI("service/stop")).build()

    @Link("restart")
    var restart: HALLink = HALLink.Builder(URI("service/restart")).build()


}

