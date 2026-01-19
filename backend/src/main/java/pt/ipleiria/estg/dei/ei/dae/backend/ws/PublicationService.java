package pt.ipleiria.estg.dei.ei.dae.backend.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.CommentDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.dtos.RatingDTO;
import pt.ipleiria.estg.dei.ei.dae.backend.ejbs.*;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Authenticated;

import java.util.List;
import java.util.Map;

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
                    ? publicationBean.getAll()
                    : publicationBean.getAllVisible();

            List<PublicationDTO> dtos = isAdminOrResponsavel
                    ? PublicationDTO.toAdminList(publications)
                    : PublicationDTO.toCollaboratorList(publications);

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
                publications.sort((p1, p2) -> Integer.compare(p2.getComments().size(), p1.getComments().size()));
                break;
            case "rating":
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
            Publication publication = publicationBean.findWithTags(id);

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
        }
    }

    // EP30: Cria uma nova publicação
    @POST
    @Path("/")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response createPublication(PublicationDTO publicationDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            // Determina o FileType com base no filename
            FileType fileType = publicationDTO.getFilename().toLowerCase().endsWith(".pdf")
                    ? FileType.PDF
                    : FileType.ZIP;

            Publication publication = publicationBean.create(
                    publicationDTO.getTitle(),
                    publicationDTO.getSummary(),
                    publicationDTO.getScientificArea(),
                    publicationDTO.getPublicationDate(),
                    publicationDTO.getAuthors(),
                    user,
                    publicationDTO.getFilename(),
                    fileType,
                    publicationDTO.getTagIds()
            );

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of("message", "Publicação com id " + publication.getId() + " criada com sucesso."))
                    .build();

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
                    publication.getTitle(),
                    publicationDTO.getSummary(),
                    publication.getScientificArea(),
                    publication.getPublicationDate(),
                    publication.getAuthors(),
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

    // EP32: Elimina uma publicação
    @DELETE
    @Path("{id}")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response deletePublication(@PathParam("id") Long id) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);

            publicationBean.delete(id, user);

            return Response.ok()
                    .entity(Map.of("message", "Publicação com id " + id + " eliminada com sucesso."))
                    .build();

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

    // EP33: Altera a visibilidade da publicação
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

    // EP34: Consulta os comentários de uma publicação
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

    // EP35: Comenta uma publicação
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

    // EP36: Altera a visibilidade de um comentário
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

    // EP37: Avalia uma publicação
    @POST
    @Path("{id}/ratings")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response ratePublication(@PathParam("id") Long id, RatingDTO ratingDTO) {
        try {
            String username = securityContext.getUserPrincipal().getName();
            User user = userBean.find(username);
            Publication publication = publicationBean.find(id);

            if (publication == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Publicação não encontrada"))
                        .build();
            }

            ratingBean.createOrUpdate(ratingDTO.getRating(), user, publication);

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

            publicationBean.addTag(publicationId, tag, user);

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

            publicationBean.removeTag(publicationId, tag, user);

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

        List<Publication> publications = publicationBean.search(searchTerm);

        // Se menos de 45 publicações, inclui o último comentário
        List<PublicationDTO> dtos = publications.size() < 45
                ? PublicationDTO.toSearchListWithComments(publications)
                : PublicationDTO.toSearchList(publications);

        return Response.ok(dtos).build();
    }

    // EP41: Ordena publicações
//    @GET
//    @Path("/")
//    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
//    public Response getSortedPublications(@QueryParam("sort") String sortBy) {
//        if (sortBy == null) {
//            return getPublications(); // EP28
//        }
//
//        boolean isAdminOrResponsavel = securityContext.isUserInRole("RESPONSAVEL") ||
//                securityContext.isUserInRole("ADMINISTRADOR");
//
//        List<Publication> publications = isAdminOrResponsavel
//                ? publicationBean.getAll()
//                : publicationBean.getAllVisible();
//
//        // Ordena conforme critério
//        switch (sortBy.toLowerCase()) {
//            case "recent":
//                publications.sort((p1, p2) -> p2.getPublicationDate().compareTo(p1.getPublicationDate()));
//                break;
//            case "comments":
//                publications.sort((p1, p2) -> Integer.compare(p2.getComments().size(), p1.getComments().size()));
//                break;
//            case "rating":
//                publications.sort((p1, p2) -> {
//                    Double avg1 = p1.getAverageRating();
//                    Double avg2 = p2.getAverageRating();
//                    if (avg1 == null) avg1 = 0.0;
//                    if (avg2 == null) avg2 = 0.0;
//                    return Double.compare(avg2, avg1);
//                });
//                break;
//            default:
//                return Response.status(Response.Status.BAD_REQUEST)
//                        .entity(Map.of("message", "Critério de ordenação inválido"))
//                        .build();
//        }
//
//        List<PublicationDTO> dtos = PublicationDTO.forSort(publications);
//        return Response.ok(dtos).build();
//    }

    // EP42: Regenera resumo com IA (placeholder - implementar integração com LLM)
    @PUT
    @Path("{id}/summary")
    @RolesAllowed({"COLABORADOR", "RESPONSAVEL", "ADMINISTRADOR"})
    public Response regenerateSummary(@PathParam("id") Long id, Map<String, Boolean> request) {
        try {
            if (request == null || !request.containsKey("regenerate")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Campo 'regenerate' é obrigatório"))
                        .build();
            }

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
                        .entity(Map.of("message", "Não tem permissão para editar esta publicação"))
                        .build();
            }

            // TODO: Integrar com serviço de IA (LLM) para gerar novo resumo
            String oldSummary = publication.getDescription();
            String newSummary = "Métodos de inteligência artificial para otimização de tarefas complexas."; // Placeholder

            publication.setDescription(newSummary);

            return Response.ok()
                    .entity(Map.of("message", "New: " + newSummary + " Old: " + oldSummary))
                    .build();

        } catch (MyEntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}