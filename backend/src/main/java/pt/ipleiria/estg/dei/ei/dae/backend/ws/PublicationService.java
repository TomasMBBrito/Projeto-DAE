package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PublicationDTO;

@Path("posts")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class PublicationService {
    @Context
    private SecurityContext securityContext;

    @GET
    @Path("/")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response GetPublication()
    {
        return Response.ok().build();
    }

    //Falta para o colaborador

    @GET
    @Path("/{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response GetPublicationById(@PathParam("id") String id)
    {
        return Response.ok().build();
    }

    @POST
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response PostPublication(PublicationDTO dto)
    {
        return Response.ok().build();
    }
}
