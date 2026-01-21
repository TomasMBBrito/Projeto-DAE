package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.EmailDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PasswordResetDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.EmailBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Hasher;
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

    @EJB
    private EmailBean emailBean;

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

    @POST
    @Path("/reset-password")
    public Response resetPassword(EmailDTO emailDTO) throws MyEntityNotFoundException {
        try {
            if (emailDTO.getEmail() == null || emailDTO.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Email é obrigatório"))
                        .build();
            }

            // Buscar user pelo email
            User user = userBean.findByMail(emailDTO.getEmail());

            if (user != null) {
                String resetToken = TokenIssuer.issue(
                        user.getUsername(),
                        user.getRole().toString()
                );

                emailBean.sendPasswordResetEmail(user.getEmail(), resetToken);


                return Response.ok()
                        .entity(Map.of("message", "Se o email existir,irá receber instruções", "token : ", resetToken))
                        .build();
            }

            return Response.ok()
                    .entity(Map.of("message", "Se o email existir, receberá instruções"))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao processar pedido"))
                    .build();
        }
    }

    @POST
    @Path("/reset-password/complete")
    @Authenticated
    public Response completePasswordReset(@Valid PasswordResetDTO resetDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            userBean.resetPassword(username, resetDTO.getPassword());

            return Response.ok()
                    .entity(Map.of("message", "Password redefinida com sucesso"))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Utilizador não encontrado"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao redefinir password"))
                    .build();
        }
    }
}
