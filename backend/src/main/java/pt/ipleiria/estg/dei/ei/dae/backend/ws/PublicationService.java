package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.RatingDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.*;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("posts")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Authenticated
public class PublicationService {
    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private RatingBean ratingBean;

    @EJB
    private UserBean userBean;

    @EJB
    private TagBean tagBean;

    @Context
    private SecurityContext securityContext;


    @GET
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getPublications(@QueryParam("sort") String sortBy) {
        if (sortBy == null) {
            boolean isAdminOrResponsavel = securityContext.isUserInRole("RESPONSAVEL") ||
                    securityContext.isUserInRole("ADMINISTRADOR");

            List<Publication> publications = isAdminOrResponsavel
                    ? publicationBean.getAllWithAllDetails()
                    : publicationBean.getAllVisibleWithAllDetails();

            List<PublicationDTO> dtos = PublicationDTO.toPublicationList(publications);

            return Response.ok(dtos).build();
        }

        //Ordena publicações
        boolean isAdminOrResponsavel = securityContext.isUserInRole("RESPONSAVEL") ||
                securityContext.isUserInRole("ADMINISTRADOR");

        List<Publication> publications = isAdminOrResponsavel
                ? publicationBean.getAll()
                : publicationBean.getAllVisible();

        // Ordena conforme critério
        switch (sortBy.toLowerCase()) {
            case "recent":
                publications.sort((p1, p2) -> p2.getPublicationDate().compareTo(p1.getPublicationDate()));
                break;
            case "comments":
                publications = isAdminOrResponsavel
                        ? publicationBean.getAllWithComments()
                        : publicationBean.getAllVisibleWithComments();
                publications.sort((p1, p2) -> Integer.compare(p2.getComments().size(), p1.getComments().size()));
                break;
            case "rating":
                publications = isAdminOrResponsavel
                        ? publicationBean.getAllWithRatings()
                        : publicationBean.getAllVisibleWithRatings();
                publications.sort((p1, p2) -> {
                    Double avg1 = p1.getAverageRating();
                    Double avg2 = p2.getAverageRating();
                    if (avg1 == null) avg1 = 0.0;
                    if (avg2 == null) avg2 = 0.0;
                    return Double.compare(avg2, avg1);
                });
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Critério de ordenação inválido"))
                        .build();
        }

        List<PublicationDTO> dtos = PublicationDTO.toSortedList(publications);
        return Response.ok(dtos).build();
    }

    // EP29: Obtém uma publicação específica
    @GET
    @Path("{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getPublication(@PathParam("id") Long id) {
        try {
            Publication publication = publicationBean.findWithAllDetails(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            if (!publicationBean.canView(publication, user)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Não tem permissão para ver esta publicação"))
                        .build();
            }

            PublicationDTO dto = PublicationDTO.forDetails(publication);
            return Response.ok(dto).build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            // Log the actual error for debugging
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Server error: " + e.getMessage()))
                    .build();
        }
    }

    // EP30: Cria uma nova publicação
    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response createPublication(MultipartFormDataInput form) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            var formData = form.getFormDataMap();

            String title = formData.get("title").get(0).getBodyAsString();

            String summary = "";
            if (formData.containsKey("summary") && !formData.get("summary").isEmpty()) {
                summary = formData.get("summary").get(0).getBodyAsString();
            }

            String scientificAreaStr = formData.get("scientificArea").get(0).getBodyAsString();
            String publicationDateStr = formData.get("publicationDate").get(0).getBodyAsString();

            var filePart = formData.get("file").get(0);
            String fileName = filePart.getFileName();
            InputStream fileInputStream = filePart.getBody(InputStream.class, null);

            // Verifica o tipo de ficheiro (PDF ou ZIP)
            FileType fileType = fileName.toLowerCase().endsWith(".pdf")
                    ? FileType.PDF
                    : FileType.ZIP;

            ScientificArea scientificArea = ScientificArea.valueOf(scientificAreaStr);
            LocalDate publicationDate = LocalDate.parse(publicationDateStr);

            List<String> authors = new ArrayList<>();
            if (formData.containsKey("authors")) {
                String authorsStr = formData.get("authors").get(0).getBodyAsString();
                if (!authorsStr.trim().isEmpty()) {
                    authorsStr = authorsStr.replaceAll("[\\[\\]\"]", "");
                    authors = Arrays.asList(authorsStr.split(","));
                }
            }

            List<Long> tagIds = new ArrayList<>();
            if (formData.containsKey("tagIds")) {
                String tagIdsStr = formData.get("tagIds").get(0).getBodyAsString();
                if (!tagIdsStr.trim().isEmpty()) {
                    tagIdsStr = tagIdsStr.replaceAll("[\\[\\]]", "");
                    tagIds = Arrays.stream(tagIdsStr.split(","))
                            .map(String::trim)
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                }
            }

            boolean ResumoaSerGerado = (summary == null || summary.trim().isEmpty());

            Publication publication = publicationBean.create(
                    title,
                    summary,
                    scientificArea,
                    publicationDate,
                    authors,
                    user,
                    fileName,
                    fileType,
                    tagIds,
                    fileInputStream
            );

            if (ResumoaSerGerado) {
                return Response.status(Response.Status.ACCEPTED) // 202 Accepted
                        .entity(Map.of(
                                "message", "Publicação criada com sucesso. O resumo está a ser gerado automaticamente em segundo plano.",
                                "id", publication.getId(),
                                "status", "processing",
                                "description", publication.getDescription()
                        ))
                        .build();
            } else {
                return Response.status(Response.Status.CREATED) // 201 Created
                        .entity(Map.of(
                                "message", "Publicação com id " + publication.getId() + " criada com sucesso.",
                                "id", publication.getId(),
                                "status", "completed"
                        ))
                        .build();
            }

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Erro ao criar publicação: " + e.getMessage()))
                    .build();
        }
    }

    // EP31: Atualiza o resumo/descrição da publicação
    @PUT
    @Path("{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updatePublication(@PathParam("id") Long id, PublicationDTO publicationDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);
            Publication publication = publicationBean.find(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            // Verifica se o utilizador pode editar
            if (!publicationBean.canEdit(publication, user)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Não tem permissão para editar esta publicação"))
                        .build();
            }

            // Atualiza apenas o resumo/descrição
            publicationBean.update(
                    id,
                    publicationDTO.getTitle(),
                    publicationDTO.getSummary(),
                    publicationDTO.getScientificArea(),
                    LocalDate.parse(publicationDTO.getPublicationDate()),
                    publicationDTO.getAuthors(),
                    user
            );

            return Response.ok()
                    .entity(Map.of("message", "Resumo da publicação com id " + id + " atualizado com sucesso."))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP32: Altera a visibilidade da publicação
    @PUT
    @Path("{id}/state")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response togglePublicationVisibility(@PathParam("id") Long id, PublicationDTO publicationDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);
            Publication publication = publicationBean.find(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            if (!publicationBean.canEdit(publication, user)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Não tem permissão para alterar a visibilidade"))
                        .build();
            }

            if (publicationDTO.isVisible()) {
                publicationBean.show(id, user);
                return Response.ok()
                        .entity(Map.of("message", "Publicação com id " + id + " foi mostrada com sucesso."))
                        .build();
            } else {
                publicationBean.hide(id, user);
                return Response.ok()
                        .entity(Map.of("message", "Publicação com id " + id + " foi ocultada com sucesso."))
                        .build();
            }

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP33: Consulta os comentários de uma publicação
    @GET
    @Path("{id}/comments")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response getPublicationComments(@PathParam("id") Long id) {
        try {
            Publication publication = publicationBean.findWithComments(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            boolean isAdminOrResponsavel = securityContext.isUserInRole("RESPONSAVEL") ||
                    securityContext.isUserInRole("ADMINISTRADOR");

            List<CommentDTO> dtos = isAdminOrResponsavel
                    ? CommentDTO.forAdminList(publication.getComments())
                    : CommentDTO.forCollaboratorList(publication.getComments());

            return Response.ok(dtos).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao obter comentários"))
                    .build();
        }
    }

    // EP34: Comenta uma publicação
    @POST
    @Path("{id}/comments")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response createComment(@PathParam("id") Long id, CommentDTO commentDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);
            Publication publication = publicationBean.find(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            commentBean.create(commentDTO.getContent(), user, publication);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Comentário criado com sucesso na publicação com id " + id + "."))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP35: Altera a visibilidade de um comentário
    @PUT
    @Path("{id}/comments/{comment_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response toggleCommentVisibility(
            @PathParam("id") Long publicationId,
            @PathParam("comment_id") Long commentId,
            CommentDTO commentDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            Comment comment = commentBean.find(commentId);
            if (comment == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Comentário não encontrado"))
                        .build();
            }

            // Verifica permissões (autor do comentário ou Responsável/Admin)
            if (!commentBean.canEdit(comment, user)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Não tem permissão para alterar este comentário"))
                        .build();
            }

            if (commentDTO.isVisible()) {
                commentBean.show(commentId, user);
                return Response.ok()
                        .entity(Map.of("message", "Comentário com id " + commentId + " mostrado com sucesso."))
                        .build();
            } else {
                commentBean.hide(commentId, user);
                return Response.ok()
                        .entity(Map.of("message", "Comentário com id " + commentId + " ocultado com sucesso."))
                        .build();
            }

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    //EP36
    @PUT
    @Path("{id}/comments/{comment_id}/text")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response updateCommentContent(
            @PathParam("id") Long publicationId,
            @PathParam("comment_id") Long commentId,
            CommentDTO commentDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            Comment comment = commentBean.find(commentId);
            if (comment == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Comentário não encontrado"))
                        .build();
            }


            if (comment.getPublication().getId() != publicationId) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Comentário não pertence a esta publicação"))
                        .build();
            }


            if (!comment.getUser().getUsername().equals(username)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("message", "Apenas o autor pode editar o comentário"))
                        .build();
            }


            if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "O conteúdo do comentário não pode estar vazio"))
                        .build();
            }

            commentBean.update(commentId, commentDTO.getContent(), user);

            return Response.ok()
                    .entity(Map.of("message", "Comentário com id " + commentId + " atualizado com sucesso."))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao atualizar comentário: " + e.getMessage()))
                    .build();
        }
    }

    // EP37: Avalia uma publicação
    @POST
    @Path("{id}/ratings")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response ratePublication(@PathParam("id") Long id, RatingDTO ratingDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            ratingBean.createOrUpdate(ratingDTO.getRating(), user, id);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Avaliação registada com sucesso na publicação com id " + id + "."))
                    .build();

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

    // EP38: Associa uma tag a uma publicação
    @POST
    @Path("{id}/tags/{tag_id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response addTagToPublication(@PathParam("id") Long publicationId, @PathParam("tag_id") Long tagId) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            Publication publication = publicationBean.find(publicationId);
            Tag tag = tagBean.find(tagId);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Tag não encontrada"))
                        .build();
            }

            publicationBean.addTag(publicationId, tagId, user);

            return Response.ok()
                    .entity(Map.of("message", "Tag com id " + tagId + " associada à publicação com id " + publicationId + " com sucesso."))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP39: Desassocia uma tag de uma publicação
    @DELETE
    @Path("{id}/tags/{tag_id}")
    @RolesAllowed({"RESPONSAVEL", "ADMINISTRADOR"})
    public Response removeTagFromPublication(@PathParam("id") Long publicationId, @PathParam("tag_id") Long tagId) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            Publication publication = publicationBean.find(publicationId);
            Tag tag = tagBean.find(tagId);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Tag não encontrada"))
                        .build();
            }

            publicationBean.removeTag(publicationId, tagId, user);

            return Response.ok()
                    .entity(Map.of("message", "Tag com id " + tagId + " desassociada da publicação com id " + publicationId + " com sucesso."))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // EP40: Pesquisa publicações
    @GET
    @Path("search")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response searchPublications(@QueryParam("q") String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Parâmetro de pesquisa 'q' é obrigatório"))
                    .build();
        }

        List<Publication> publications;
        List<PublicationDTO> dtos;

        if (publicationBean.search(searchTerm).size() < 45) {
            publications = publicationBean.searchWithComments(searchTerm);
            dtos = PublicationDTO.toSearchListWithComments(publications);
        } else {
            publications = publicationBean.search(searchTerm);
            dtos = PublicationDTO.toSearchList(publications);
        }

        return Response.ok(dtos).build();
    }

    //EP41
    @GET
    @Path("filter/tags")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response filterByTags(@QueryParam("tagIds") String tagIdsParam) {
        try {
            if (tagIdsParam == null || tagIdsParam.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Parâmetro 'tagIds' é obrigatório"))
                        .build();
            }

            // Parse tag IDs from comma-separated string
            List<Long> tagIds = Arrays.stream(tagIdsParam.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            boolean isAdminOrResponsavel = securityContext.isUserInRole("RESPONSAVEL") ||
                    securityContext.isUserInRole("ADMINISTRADOR");

            List<Publication> publications = isAdminOrResponsavel
                    ? publicationBean.filterByTags(tagIds)
                    : publicationBean.filterByTagsVisible(tagIds);

            List<PublicationDTO> dtos = PublicationDTO.toSearchList(publications);
            return Response.ok(dtos).build();

        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "IDs de tags inválidos"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Erro ao filtrar publicações"))
                    .build();
        }
    }
}