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
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class TagService {

    @EJB
    private TagBean tagBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;


    @GET
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response listTags() {
        try {
            //String role = securityContext.getUserPrincipal().getName();
            boolean isAdmin = securityContext.isUserInRole("RESPONSAVEL") ||
                    securityContext.isUserInRole("ADMINISTRADOR");

            List<Tag> tags;
            List<TagDTO> tagDTOs;
            if (isAdmin) {
                tags = tagBean.getAll();
                tagDTOs = TagDTO.from(tags);
                return Response.ok(tagDTOs).build();
            } else {
                tags = tagBean.getAllVisible();
                tagDTOs = TagDTO.fromSimple(tags);
                return Response.ok(tagDTOs).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving tags: " + e.getMessage())
                    .build();
        }
    }


    @GET
    @Path("/{tag_id}/posts")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTagPublications(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.findWithPublications(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Tag not found"))
                        .build();
            }

            List<PublicationDTO> publications = PublicationDTO.toUserPostsList(tag.getPublications());

            return Response.ok(publications).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving publications: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{tag_id}/subscribers")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTagSubscribers(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.findWithSubscribers(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Tag not found"))
                        .build();
            }

            List<UserDTO> subscribers = UserDTO.from(tag.getSubscribers());

            return Response.ok(subscribers).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving subscribers: " + e.getMessage())
                    .build();
        }
    }


    @POST
    @Path("/")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response createTag(TagDTO tagDTO) {
        try {
            if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Tag name is required")
                        .build();
            }

            if (tagBean.exists(tagDTO.getName())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Tag with this name already exists")
                        .build();
            }

            String username = securityContext.getUserPrincipal().getName();
            User performedBy = userBean.find(username);

            Tag tag = tagBean.create(tagDTO.getName(), performedBy);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message","Tag com id " + tag.getId() + " criado com sucesso"))
                    .build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Error creating tag: " + e.getMessage()))
                    .build();
        }
    }


    @PUT
    @Path("/{tag_id}")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateTag(@PathParam("tag_id") Long tagId, TagDTO tagDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            Tag updatedTag = tagBean.update(tagId, tagDTO.getName(), username);
            TagDTO tagDTO_res = TagDTO.from(updatedTag);
            return Response.ok()
                    .entity(Map.of(
                            "message", "Tag updated successfully",
                            "tag", tagDTO_res
                    ))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Error updating tag: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{tag_id}")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response deleteTag(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            String username = securityContext.getUserPrincipal().getName();

            //tagBean.delete(tagId, performedBy);
            tagBean.hide(tagId,username);

            return Response.ok()
                    .entity(Map.of("message", "Tag eliminada com sucesso"))
                    .build();
        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Error deleting tag: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{tag_id}/status")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response UpdateTagStatus(@PathParam("tag_id") Long tagId, TagDTO tagDTO) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Tag não encontrada"))
                        .build();
            }

            String username = securityContext.getUserPrincipal().getName();

            if (tagDTO.isVisible()) {
                tagBean.show(tagId, username);
                return Response.ok()
                        .entity(Map.of("message", "Tag com id " + tagId + " foi mostrada com sucesso"))
                        .build();
            } else {
                tagBean.hide(tagId, username);
                return Response.ok()
                        .entity(Map.of("message", "Tag com id " + tagId + " foi ocultada com sucesso"))
                        .build();
            }

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao alterar visibilidade da tag: " + e.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/{tag_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTag(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.findWithDetails(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            TagDTO tagDTO = TagDTO.from(tag);
            return Response.ok(tagDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Error retrieving tag: " + e.getMessage()))
                    .build();
        }
    }
}