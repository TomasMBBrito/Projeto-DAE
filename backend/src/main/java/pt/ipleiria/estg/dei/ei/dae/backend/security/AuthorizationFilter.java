package pt.ipleiria.estg.dei.ei.dae.backend.security;

import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.core.ResourceMethodInvoker;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Filtro que verifica se o utilizador autenticado tem permissão para aceder ao recurso
 * Executa após a autenticação (prioridade AUTHORIZATION)
 *
 * Regras de precedência:
 * - Anotações no metodo têm precedência sobre anotações na classe
 * - @PermitAll, @DenyAll, @RolesAllowed podem ser combinadas
 */
@Provider
@Authenticated
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    private static final Response ACCESS_DENIED = Response.status(401)
            .entity("Access denied for this resource").build();

    private static final Response ACCESS_FORBIDDEN = Response.status(403)
            .entity("Access forbidden for this resource").build();

    @Context
    private SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) containerRequestContext
                .getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");

        Method method = methodInvoker.getMethod();
        var resource = method.getDeclaringClass();

        // Cenário 1: Classe com @PermitAll
        if (resource.isAnnotationPresent(PermitAll.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                containerRequestContext.abortWith(ACCESS_DENIED);
                return;
            }

            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                HashSet<String> roles = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                if (roles.stream().anyMatch(securityContext::isUserInRole)) {
                    return;
                }
                containerRequestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }
            return; // PermitAll - acesso concedido
        }

        // Cenário 2: Classe com @DenyAll
        if (resource.isAnnotationPresent(DenyAll.class)) {
            if (method.isAnnotationPresent(PermitAll.class)) {
                return;
            }

            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed beanRolesAnnotation = method.getAnnotation(RolesAllowed.class);
                HashSet<String> roles = new HashSet<>(Arrays.asList(beanRolesAnnotation.value()));

                if (roles.stream().anyMatch(securityContext::isUserInRole)) {
                    return;
                }
            }

            containerRequestContext.abortWith(ACCESS_DENIED);
            return;
        }

        // Cenário 3: Classe com @RolesAllowed
        if (resource.isAnnotationPresent(RolesAllowed.class)) {
            if (method.isAnnotationPresent(DenyAll.class)) {
                containerRequestContext.abortWith(ACCESS_DENIED);
                return;
            }

            if (method.isAnnotationPresent(PermitAll.class)) {
                return;
            }

            RolesAllowed rolesAnnotation = resource.getAnnotation(RolesAllowed.class);
            HashSet<String> roles = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

            // Adicionar roles do metodo (se existirem)
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                roles.addAll(Arrays.asList(rolesAnnotation.value()));
            }

            if (roles.stream().anyMatch(securityContext::isUserInRole)) {
                return;
            }

            containerRequestContext.abortWith(ACCESS_FORBIDDEN);
            return;
        }

        // Cenário 4: Sem anotações na classe, verificar método
        if (method.isAnnotationPresent(DenyAll.class)) {
            containerRequestContext.abortWith(ACCESS_DENIED);
            return;
        }

        if (method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        if (method.isAnnotationPresent(RolesAllowed.class)) {
            HashSet<String> roles = new HashSet<>(Arrays.asList(
                    method.getAnnotation(RolesAllowed.class).value()));

            if (roles.stream().anyMatch(securityContext::isUserInRole)) {
                return;
            }

            containerRequestContext.abortWith(ACCESS_FORBIDDEN);
        }
    }
}