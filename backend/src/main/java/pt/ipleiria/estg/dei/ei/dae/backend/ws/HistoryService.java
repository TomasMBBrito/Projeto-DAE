package pt.ipleiria.estg.dei.ei.dae.backend.ws;


import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.HistoryDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.HistoryBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.History;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;

import java.util.List;
import java.util.Map;

@Path("history")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class HistoryService {

    @EJB
    private HistoryBean historyBean;

    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    @Context
    private SecurityContext securityContext;


    @GET
    @Path("/{username}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response getUserHistory(@PathParam("username") String username) {
        try {
            userBean.find(username);

            List<History> activities = historyBean.getUserHistory(username);
            List<HistoryDTO> dtos = HistoryDTO.from(activities);

            return Response.ok(dtos).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Utilizador não encontrado"))
                    .build();
        }
    }


    @GET
    @Path("/me")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getMyHistory() {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();

            List<History> activities = historyBean.getUserHistory(currentUsername);
            List<HistoryDTO> dtos = HistoryDTO.from(activities);

            return Response.ok(dtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao obter histórico"))
                    .build();
        }
    }


    @GET
    @Path("/me/{post_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getMyPublicationHistory(@PathParam("post_id") Long postId) {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();
            User currentUser = userBean.find(currentUsername);

            Publication publication = publicationBean.find(postId);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }


            if (!publication.getAuthor().getUsername().equals(currentUsername)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Não tem permissão para ver o histórico desta publicação"))
                        .build();
            }

            List<History> activities = historyBean.getPublicationHistory(postId);
            List<HistoryDTO> dtos = HistoryDTO.fromWithUser(activities);

            return Response.ok(dtos).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/posts/{post_id}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response getPublicationHistory(@PathParam("post_id") Long postId) {
        try {
            Publication publication = publicationBean.find(postId);
            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            List<History> activities = historyBean.getPublicationHistory(postId);
            List<HistoryDTO> dtos = HistoryDTO.fromWithUser(activities);

            return Response.ok(dtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao obter histórico"))
                    .build();
        }
    }
}
