package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyConstraintViolationException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Path("users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class UserService {

    @EJB
    private UserBean userBean;

    @EJB
    private PublicationBean publicationBean;

    @Context
    private SecurityContext securityContext;


    @GET
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getUsers() throws MyEntityNotFoundException {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);
            return Response.ok(UserDTO.from(userBean.getAll(user))).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR"})
    @Path("/{username}")
    public Response getUser(@PathParam("username") String username) throws MyEntityNotFoundException {
        try {
            User user = userBean.find(username);
            return Response.ok(UserDTO.from(user)).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "User " + username + " not found"))
                    .build();
        }
    }

    @DELETE
    @RolesAllowed({"ADMINISTRADOR"})
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String username) {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();
            User performingUser = userBean.find(currentUsername);

            //userBean.delete(username, performingUser);
            if(userBean.hasActivity(username)) {
                userBean.deactivate(username,performingUser);
            }else{
                userBean.delete(username, performingUser);
            }

            return Response.ok(Map.of(
                    "message", "O utilizador " + username + " foi eliminado com sucesso"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{username}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response updateUser(@PathParam("username") String username,UserDTO userDTO) throws MyEntityNotFoundException {
        try {
            userBean.update(username, userDTO.getEmail(), userDTO.getName());
            return Response.ok(Map.of(
                    "message", "Dados de " + username + " atualizados com sucesso"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @RolesAllowed({"ADMINISTRADOR"})
    @Path("/")
    public Response createUser(UserDTO userDTO) {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();
            User performingUser = userBean.find(currentUsername);

            User createdUser = userBean.create(
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getEmail(),
                    userDTO.getName(),
                    userDTO.getRole(),
                    performingUser
            );

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "message", "Utilizador com username " + createdUser.getUsername() + " criado com sucesso"
                    ))
                    .build();
        } catch (MyEntityExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (MyConstraintViolationException | MyEntityNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{username}/role")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response changeUserRole(@PathParam("username") String username, UserDTO userDTO) throws MyEntityNotFoundException {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();
            User performingUser = userBean.find(currentUsername);

            userBean.changeRole(username, userDTO.getRole(), performingUser);
            return Response.ok(Map.of("message", "Role atualizado !!")).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{username}/status")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response changeUserStatus(@PathParam("username") String username, UserDTO userDTO) throws MyEntityNotFoundException {
        try {
            String currentUsername = securityContext.getUserPrincipal().getName();
            User performingUser = userBean.find(currentUsername);

            if (userDTO.isBlocked()) {
                userBean.deactivate(username, performingUser);
            } else {
                userBean.activate(username, performingUser);
            }

            return Response.ok(Map.of("message", "Status do utilizador atualizado !!")).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    @Path("/{username}/posts")
    public Response getUserPosts(@PathParam("username") String username) throws MyEntityNotFoundException {
        try {
            List<Publication> posts = publicationBean.getByUser(username);
            return Response.ok(PublicationDTO.toAdminList(posts)).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/me/posts")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getMyPosts() {
        try {
            String username = securityContext.getUserPrincipal().getName();
            List<Publication> posts = publicationBean.getByUser(username);
            return Response.ok(PublicationDTO.toUserPostsList(posts)).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/me/posts/{postId}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response deleteMyPost(@PathParam("postId") Long postId) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User myUser = userBean.find(username);

            publicationBean.delete(postId, myUser);
            return Response.ok(Map.of(
                    "message", "O seu post com id " + postId + " foi eliminado com sucesso!!"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateMyUser(UserDTO userDTO) throws MyEntityNotFoundException {
        try {
            String username = securityContext.getUserPrincipal().getName();
            userBean.update(username, userDTO.getEmail(), userDTO.getName());
            return Response.ok(Map.of(
                    "message", "Os seus dados foram atualizados com sucesso"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/me/password")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateMyPassword(UserDTO userDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            userBean.changePassword(username, userDTO.getOldPassword(), userDTO.getNewPassword());
            return Response.ok(Map.of(
                    "message", "Password atualizada com sucesso !!"
            )).build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}
