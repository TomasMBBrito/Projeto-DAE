package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;

import java.util.List;
import java.util.Map;

@Path("me/tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class SubscriptionService {

    @EJB
    private TagBean tagBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response subscribeTag(TagDTO dto) {
        String currentUsername = securityContext.getUserPrincipal().getName();
        try {
            userBean.subscribeTag(currentUsername, dto.getId());
            return Response.ok(Map.of(
                    "message", "Tag com id " + dto.getId() + " subscrita com sucesso!"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getSubscribedTags() {
        String currentUsername = securityContext.getUserPrincipal().getName();
        try {
            List<TagDTO> tags = TagDTO.from(tagBean.getSubscribedTags(currentUsername));
            return Response.ok(tags).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("{tag_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response unsubscribeTag(@PathParam("tag_id") Long tagId) {
        String currentUsername = securityContext.getUserPrincipal().getName();
        try {
            userBean.unsubscribeTag(currentUsername, tagId);
            return Response.ok(Map.of(
                    "message", "Subscrição da tag com id " + tagId + " anulada com sucesso!"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}
