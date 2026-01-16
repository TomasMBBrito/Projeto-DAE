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

import java.util.List;
import java.util.stream.Collectors;

@Path("/tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class TagService {

    @EJB
    private TagBean tagBean;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    // EP21: List all tags
    @GET
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response listTags() {
        try {
            String role = securityContext.getUserPrincipal().getName();
            boolean isAdmin = securityContext.isUserInRole("RESPONSAVEL") ||
                    securityContext.isUserInRole("ADMINISTRADOR");

            List<Tag> tags;
            if (isAdmin) {
                // Responsável or Administrador: return all tags with visibility info
                tags = tagBean.getAll();
                List<TagDTO> tagDTOs = tags.stream()
                        .map(tag -> new TagDTO(tag.getId(), tag.getName(), tag.isVisible(), 0, 0))
                        .collect(Collectors.toList());
                return Response.ok(tagDTOs).build();
            } else {
                // Colaborador: return only visible tags without visibility field
                tags = tagBean.getAllVisible();
                List<TagDTO> tagDTOs = tags.stream()
                        .map(TagDTO::fromSimple)
                        .collect(Collectors.toList());
                return Response.ok(tagDTOs).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving tags: " + e.getMessage())
                    .build();
        }
    }

    // EP22: Get publications associated with a tag
    @GET
    @Path("/{tag_id}/posts")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTagPublications(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            List<PublicationDTO> publications = tag.getPublications().stream()
                    .map(pub -> new PublicationDTO(
                            pub.getId(),
                            pub.getTitle(),
                            pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                            pub.isVisible()
                    ))
                    .collect(Collectors.toList());

            return Response.ok(publications).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving publications: " + e.getMessage())
                    .build();
        }
    }

    // EP23: Get subscribers of a tag
    @GET
    @Path("/{tag_id}/subscribers")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTagSubscribers(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            List<UserDTO> subscribers = tag.getSubscribers().stream()
                    .map(user -> new UserDTO(
                            user.getUsername(),
                            user.getEmail(),
                            user.getName(),
                            user.getRole(),
                            user.isBlocked()
                    ))
                    .collect(Collectors.toList());

            return Response.ok(subscribers).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving subscribers: " + e.getMessage())
                    .build();
        }
    }

    // EP24: Create a new tag
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
                    .entity(new MessageResponse("Tag com id " + tag.getId() + " criado com sucesso"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating tag: " + e.getMessage())
                    .build();
        }
    }

    // EP25: Update tag name
    @PUT
    @Path("/{tag_id}")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateTag(@PathParam("tag_id") Long tagId, TagDTO tagDTO) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            if (tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Tag name is required")
                        .build();
            }

            String username = securityContext.getUserPrincipal().getName();
            User performedBy = userBean.find(username);

            tag.setName(tagDTO.getName());

            return Response.ok()
                    .entity(new MessageResponse("Nome da tag com id " + tagId + " atualizada com com sucesso"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating tag: " + e.getMessage())
                    .build();
        }
    }

    // EP26: Delete a tag
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
            User performedBy = userBean.find(username);

            tagBean.delete(tagId, performedBy);

            return Response.ok()
                    .entity(new MessageResponse("Tag eliminada com sucesso"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting tag: " + e.getMessage())
                    .build();
        }
    }

    // EP27: Get specific tag by ID
    @GET
    @Path("/{tag_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getTag(@PathParam("tag_id") Long tagId) {
        try {
            Tag tag = tagBean.find(tagId);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tag not found")
                        .build();
            }

            TagDTO tagDTO = new TagDTO(tag.getId(), tag.getName());
            return Response.ok(tagDTO).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving tag: " + e.getMessage())
                    .build();
        }
    }

    // Helper class for message responses
    private static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}