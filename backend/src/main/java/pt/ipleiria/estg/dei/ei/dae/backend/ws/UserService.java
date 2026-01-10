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

    private Principal principal = securityContext.getUserPrincipal();

    @GET
    @Path("/")
    public List<UserDTO> getUsers() throws MyEntityNotFoundException {
        String username = principal.getName();
        User user = userBean.find(username);
        return UserDTO.from(userBean.getAll(user));
    }

    @GET
    @RolesAllowed({"Administrador"})
    @Path("/{username}")
    public Response getUser(@PathParam("username") String username) throws MyEntityNotFoundException {
//        if(!securityContext.isUserInRole("ADMINISTRADOR")){
//            return Response.status(Response.Status.FORBIDDEN).build();
//        }
        User user = userBean.find(username);
        UserDTO userDTO = UserDTO.from(user);
        return Response.ok(userDTO).build();
    }

    @DELETE
    @RolesAllowed({"ADMINISTRADOR"})
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String username) throws MyEntityNotFoundException {
        User user_performing = userBean.find(principal.getName());
        userBean.delete(username,user_performing);
        return Response.ok("Utilizador " + username + " eliminado com sucesso").build();
    }

    @PUT
    @Path("/{username}")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response updateUser(@PathParam("username") String username,UserDTO userDTO) throws MyEntityNotFoundException {
        userBean.update(username,userDTO.getEmail(),userDTO.getName());
        return Response.ok("Dados de " + username + " atualizados com sucesso").build();
    }

    @POST
    @RolesAllowed({"ADMINISTRADOR"})
    @Path("/")
    public Response createUser(UserDTO userDTO) throws MyEntityNotFoundException, MyConstraintViolationException, MyEntityExistsException {
        User user_performing = userBean.find(principal.getName());
        User createdUser = userBean.create(userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail(), userDTO.getName(), userDTO.getRole(), user_performing);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                        "message", "Utilizador criado com sucesso",
                        "user", UserDTO.from(createdUser)
                )).build();
    }

    @PUT
    @Path("/{username}/role")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response changeUserRole(@PathParam("username") String username, UserDTO userDTO) throws MyEntityNotFoundException {
        User user_performing = userBean.find(principal.getName());
        userBean.changeRole(username, userDTO.getRole(), user_performing);
        return Response.ok("Role atualizado").build();
    }

    @PUT
    @Path("/{username}/status")
    @RolesAllowed({"ADMINISTRADOR"})
    public Response changeUserStatus(@PathParam("username") String username, UserDTO userDTO) throws MyEntityNotFoundException {
        User user_performing = userBean.find(principal.getName());
        if(userDTO.isActive()){
            userBean.activate(username, user_performing);
        } else {
            userBean.deactivate(username, user_performing);
        }
        return Response.ok("Status atualizado").build();
    }

    @GET
    @RolesAllowed({"ADMINISTRADOR", "RESPONSAVEL"})
    @Path("/{username}/posts")
    public Response getUserPosts(@PathParam("username") String username) throws MyEntityNotFoundException {
        List<Publication> posts = publicationBean.getByUser(username);
        return Response.ok(PublicationDTO.from(posts)).build();
    }

    @GET
    @Path("/me/posts")
    public Response getMyPosts() throws MyEntityNotFoundException {
        String username = principal.getName();
        List<Publication> posts = publicationBean.getByUser(username);
        return Response.ok(PublicationDTO.from(posts)).build();
    }

    @DELETE
    @Path("/me/posts/{postId}")
    public Response deleteMyPost(@PathParam("postId") Long postId) throws MyEntityNotFoundException {
        String username = principal.getName();
        User MyUser = userBean.find(username);
        publicationBean.delete(postId, MyUser);
        return Response.ok().entity(Map.of(
                "message", "Publicação eliminada com sucesso",
                "id", postId
        )).build();
    }

    @PUT
    @Path("/me")
    public Response updateMyUser(UserDTO userDTO) throws MyEntityNotFoundException {
        String username = principal.getName();
        userBean.update(username, userDTO.getEmail(), userDTO.getName());
        return Response.ok("Os seus dados foram atualizados com sucesso").build();
    }

    @PUT
    @Path("/me/password")
    public Response updateMyPassword(UserDTO userDTO) throws MyEntityNotFoundException {
        String username = principal.getName();
        userBean.changePassword(username, userDTO.getPassword());
        return Response.ok("A sua palavra-passe foi atualizada com sucesso").build();
    }
}
