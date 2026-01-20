package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.backend.security.TokenIssuer;

import java.security.Principal;
import java.util.Map;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthService {

    @Context
    private SecurityContext securityContext;

    @EJB
    private UserBean userBean;

    @POST
    @Path("/login")
    public Response login(@Valid AuthDTO authDTO) throws MyEntityNotFoundException {
        if(userBean.canLogin(authDTO.getUsername(),authDTO.getPassword())){
            User user = userBean.find(authDTO.getUsername());
            String token = TokenIssuer.issue(authDTO.getUsername(),user.getRole().toString());
            return Response.ok(UserDTO.from(user))
                    .header("Authorization", "Bearer " + token)
                    .build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Authenticated
    @Path("/user")
    public Response getUser() throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        User user = userBean.find(username);
        return Response.ok(UserDTO.from(user)).build();
    }

    @GET
    @Path("/debug")
    @Authenticated
    public Response debug() {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = securityContext.isUserInRole("ADMINISTRADOR");
        boolean isColaborador = securityContext.isUserInRole("COLABORADOR");
        boolean isResponsavel = securityContext.isUserInRole("RESPONSAVEL");

        return Response.ok(Map.of(
                "username", username,
                "isADMINISTRADOR", isAdmin,
                "isCOLABORADOR", isColaborador,
                "isRESPONSAVEL", isResponsavel
        )).build();
    }
}
