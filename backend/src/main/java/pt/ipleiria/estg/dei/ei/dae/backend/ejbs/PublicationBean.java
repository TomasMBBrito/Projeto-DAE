package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ejb.Asynchronous;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.services.AIService;
import pt.ipleiria.estg.dei.ei.dae.backend.utils.PdfTextExtractor;
import pt.ipleiria.estg.dei.ei.dae.backend.utils.ZipTextExtractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

@Stateless
public class PublicationBean {

    private static final Logger logger = Logger.getLogger(PublicationBean.class.getName());

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;

    @EJB
    private UserBean userBean;

    @EJB
    private TagBean tagBean;

    @EJB
    private EmailBean emailBean;

    @EJB
    private DocumentBean documentBean;

    @Inject
    private AIService aiService;

    public Publication create(String title, String description, ScientificArea scientificArea,
                              LocalDate publicationDate, List<String> authors, User submitter,
                              String fileName, FileType fileType,List<Long> tagIds,InputStream fileInputStream) throws MyEntityNotFoundException, IOException {

        if (submitter == null) {
            throw new MyEntityNotFoundException("Submitter user not found");
        }

        byte[] fileBytes = fileInputStream.readAllBytes();

        InputStream streamForDocument = new java.io.ByteArrayInputStream(fileBytes);

        Document document = documentBean.create(fileName, submitter.getUsername(), streamForDocument, fileType);

        String finalDescription = description;
        boolean needsAiGeneration = (description == null || description.trim().isEmpty());

        if (needsAiGeneration) {
            finalDescription = "A gerar resumo... Por favor aguarde";
        }

//        if(description == null || description.trim().isEmpty()) {
//            try {
//                if (fileType == FileType.PDF) {
//                    InputStream streamForAI = new ByteArrayInputStream(fileBytes);
//                    String pdfText = PdfTextExtractor.extractText(streamForAI);
//
//                    if (pdfText != null && !pdfText.trim().isEmpty()) {
//
//                        String aiSummary = aiService.generateSummary(pdfText);
//
//                        if (aiSummary != null && !aiSummary.trim().isEmpty()) {
//                            finalDescription = aiSummary;
//                        } else {
//                            finalDescription = "Resumo automático não disponível - Por favor adicione uma descrição manualmente";
//                        }
//                    } else {
//                        finalDescription = "Não foi possível extrair texto do PDF";
//                    }
//
//                } else if (fileType == FileType.ZIP) {
//                    InputStream streamForZip = new ByteArrayInputStream(fileBytes);
//                    String zipText = ZipTextExtractor.extractTextFromPDFs(streamForZip);
//
//                    if (zipText != null && !zipText.trim().isEmpty()) {
//
//                        String aiSummary = aiService.generateSummary(zipText);
//
//                        if (aiSummary != null && !aiSummary.trim().isEmpty()) {
//                            finalDescription = aiSummary;
//                        } else {
//                            finalDescription = "Resumo automático não disponível - Por favor adicione uma descrição manualmente";
//                        }
//                    } else {
//                        finalDescription = "Ficheiro ZIP - nenhum PDF encontrado para análise";
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                finalDescription = "Resumo não disponível (erro ao processar: " + e.getMessage() + ")";
//            }
//        }

        // Create publication
        Publication publication = new Publication(
                title,
                finalDescription,
                scientificArea,
                document,
                publicationDate,
                submitter
        );

        publication.setAuthors(authors);
        publication.setDocument(document);
        document.setPublication(publication);

        em.persist(publication);
        em.flush(); // Ensure publication gets an ID before creating document

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                Tag tag = tagBean.find(tagId);
                if (tag != null) {
                    publication.addTag(tag);
                }
            }
        }

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_CREATED,
                "Publication created: " + title + " with file: " + fileName,
                "Publication",
                publication.getId(),
                submitter
        );

        if (needsAiGeneration) {
            logger.info("Iniciando geração assíncrona de resumo para publicação ID: " + publication.getId());
            generateSummaryAsync(publication.getId(), fileBytes, fileType, submitter.getUsername());
        }

        return publication;
    }

    //Metodo apenas para criar publicações de teste sem ficheiro
    public Publication createWithoutFile(String title, String description, ScientificArea scientificArea,
                                         LocalDate publicationDate, List<String> authors, User submitter,
                                         String fileName, FileType fileType, List<Long> tagIds)
            throws MyEntityNotFoundException {

        if (submitter == null) {
            throw new MyEntityNotFoundException("Submitter user not found");
        }


        Document document = new Document(fileName, "dummy_path", fileType);

        // Criar publicação
        Publication publication = new Publication(
                title,
                description,
                scientificArea,
                document,
                publicationDate,
                submitter
        );

        publication.setAuthors(authors);
        publication.setDocument(document);
        document.setPublication(publication);

        em.persist(publication);
        em.flush();

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                Tag tag = em.find(Tag.class, tagId);
                if (tag != null) {
                    publication.addTag(tag);
                }
            }
        }

        historyBean.logActivity(
                ActivityType.PUBLICATION_CREATED,
                "Publication created: " + title + " (test data - no physical file)",
                "Publication",
                publication.getId(),
                submitter
        );

        return publication;
    }


    public Publication find(Long id) {
        return em.find(Publication.class, id);
    }

    public Publication findWithTags(Long id) {
        Publication pub = find(id);
        if (pub != null) {
            Hibernate.initialize(pub.getTags());
        }
        return pub;
    }

    public Publication findWithComments(Long id) {
        Publication pub = find(id);
        if (pub != null) {
            Hibernate.initialize(pub.getComments());
            pub.getComments().forEach(comment ->
                    Hibernate.initialize(comment.getUser())
            );
        }
        return pub;
    }

    public Publication findWithAllDetails(Long id) {
        Publication pub = find(id);
        if (pub != null) {
            // Initialize all lazy-loaded collections to avoid LazyInitializationException
            Hibernate.initialize(pub.getTags());
            Hibernate.initialize(pub.getComments());
            Hibernate.initialize(pub.getRatings());
            Hibernate.initialize(pub.getAuthors());
        }
        return pub;
    }

    public List<Publication> getAll() {
        return em.createNamedQuery("getAllPublications", Publication.class).getResultList();
    }

    public List<Publication> getAllVisible() {
        return em.createNamedQuery("getVisiblePublications", Publication.class).getResultList();
    }

    public List<Publication> getAllWithAllDetails() {
        List<Publication> publications = getAll();
        publications.forEach(p -> {
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getTags());
        });
        return publications;
    }

    public List<Publication> getAllVisibleWithAllDetails() {
        List<Publication> publications = getAllVisible();
        publications.forEach(p -> {
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getTags());
        });
        return publications;
    }

    public List<Publication> getAllWithComments() {
        List<Publication> publications = getAll();
        publications.forEach(p -> Hibernate.initialize(p.getComments()));
        return publications;
    }

    public List<Publication> getAllVisibleWithComments() {
        List<Publication> publications = getAllVisible();
        publications.forEach(p -> Hibernate.initialize(p.getComments()));
        return publications;
    }

    public List<Publication> getAllWithRatings() {
        List<Publication> publications = getAll();
        publications.forEach(p -> Hibernate.initialize(p.getRatings()));
        return publications;
    }

    public List<Publication> getAllVisibleWithRatings() {
        List<Publication> publications = getAllVisible();
        publications.forEach(p -> Hibernate.initialize(p.getRatings()));
        return publications;
    }



    public List<Publication> getByUser(String username) throws MyEntityNotFoundException {

        if(em.find(User.class, username) == null){
            throw new MyEntityNotFoundException("User not found: " + username);
        }
        List<Publication> publications = em.createQuery(
                        "SELECT p FROM Publication p WHERE p.author.username = :username ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("username", username)
                .getResultList();

        publications.forEach(p -> {
            Hibernate.initialize(p.getComments());
            Hibernate.initialize(p.getRatings());
            Hibernate.initialize(p.getTags());
        });

        return publications;
    }

    public List<Publication> getByTag(String tagName) {
        return em.createQuery(
                        "SELECT p FROM Publication p JOIN p.tags t WHERE t.name = :tagName AND p.visible = true ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("tagName", tagName)
                .getResultList();
    }

    public List<Publication> search(String searchTerm) {
        return em.createQuery(
                        "SELECT p FROM Publication p WHERE (LOWER(p.title) LIKE LOWER(:search) OR LOWER(p.description) LIKE LOWER(:search)) AND p.visible = true ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("search", "%" + searchTerm + "%")
                .getResultList();
    }

    public List<Publication> searchWithComments(String searchTerm) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Publication p " +
                                "LEFT JOIN FETCH p.comments " +
                                "WHERE (LOWER(p.title) LIKE LOWER(:search) OR LOWER(p.description) LIKE LOWER(:search)) " +
                                "AND p.visible = true " +
                                "ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("search", "%" + searchTerm + "%")
                .getResultList();
    }

    public List<Publication> getByScientificArea(ScientificArea area) {
        return em.createQuery(
                        "SELECT p FROM Publication p WHERE p.scientificArea = :area AND p.visible = true ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("area", area)
                .getResultList();
    }

    public List<Publication> filterByTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        // Retorna publicações que têm TODAS as tags selecionadas
        return em.createQuery(
                        "SELECT p FROM Publication p " +
                                "JOIN p.tags t " +
                                "WHERE t.id IN :tagIds " +
                                "GROUP BY p " +
                                "HAVING COUNT(DISTINCT t.id) = :tagCount " +
                                "ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("tagIds", tagIds)
                .setParameter("tagCount", (long) tagIds.size())
                .getResultList();
    }

    public List<Publication> filterByTagsVisible(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        // Igual ao acima mas apenas publicações visíveis
        return em.createQuery(
                        "SELECT p FROM Publication p " +
                                "JOIN p.tags t " +
                                "WHERE t.id IN :tagIds " +
                                "AND p.visible = true " +
                                "GROUP BY p " +
                                "HAVING COUNT(DISTINCT t.id) = :tagCount " +
                                "ORDER BY p.publicationDate DESC",
                        Publication.class
                )
                .setParameter("tagIds", tagIds)
                .setParameter("tagCount", (long) tagIds.size())
                .getResultList();
    }


    public void update(Long id, String title, String description, ScientificArea scientificArea,
                       LocalDate publicationDate, List<String> authors, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + id);
        }
        em.lock(publication, LockModeType.OPTIMISTIC);
        publication.setTitle(title);
        publication.setDescription(description);
        publication.setScientificArea(scientificArea);
        publication.setPublicationDate(publicationDate);
        publication.setAuthors(authors);

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_UPDATED,
                "Publication updated: " + title,
                "Publication",
                id,
                performedBy
        );
    }


    public void updateDocument(Long publicationId, String fileName, FileType fileType,
                               InputStream fileContent, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + publicationId);
        }

        // Delete old document if exists
        if (publication.getDocument() != null) {
            Long oldDocId = publication.getDocument().getId();
            //documentBean.delete(oldDocId);

        }

        // Create new document
        Document newDocument = new Document(fileName, "home" ,fileType);
        publication.setDocument(newDocument);

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_UPDATED,
                "Document replaced for publication: " + publication.getTitle(),
                "Publication",
                publicationId,
                performedBy
        );
    }


    public void hide(Long id, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + id);
        }

        publication.setVisible(false);

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_HIDDEN,
                "Publication hidden: " + publication.getTitle(),
                "Publication",
                id,
                performedBy
        );
    }

    public void show(Long id, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + id);
        }

        publication.setVisible(true);

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_SHOWN,
                "Publication shown: " + publication.getTitle(),
                "Publication",
                id,
                performedBy
        );
    }


    public void delete(Long id, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + id);
        }

        if(em.find(User.class, performedBy.getUsername()) == null) {
            throw new MyEntityNotFoundException("User not found: " + performedBy.getUsername());
        }

        if(!canDelete(publication, performedBy)) {
            throw new IllegalArgumentException("User " + performedBy.getUsername() + " does not have permission to delete this publication.");
        }

        String title = publication.getTitle();

        // Delete document first (cascade will handle it, but we log it explicitly)
        if (publication.getDocument() != null) {
            //documentBean.delete(publication.getDocument().getId());
        }

        // Log activity BEFORE deletion
        historyBean.logActivity(
                ActivityType.PUBLICATION_DELETED,
                "Publication deleted: " + title,
                "Publication",
                id,
                performedBy
        );

        em.remove(publication);
    }


    public void addTag(Long publicationId, Long tagId, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + publicationId);
        }

        Tag tag = tagBean.find(tagId);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found: " + tagId);
        }

        publication.addTag(tag);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_ADDED_TO_PUBLICATION,
                "Tag '" + tag.getName() + "' added to publication: " + publication.getTitle(),
                "Publication",
                publicationId,
                performedBy
        );

        // Notify subscribers of this tag
        emailBean.notifyTagSubscribers(tag, publication, "New publication with tag: " + tag.getName());
    }

    public void removeTag(Long publicationId, Long tagId, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + publicationId);
        }

        Tag tag = tagBean.find(tagId);

        publication.removeTag(tag);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_REMOVED_FROM_PUBLICATION,
                "Tag '" + tag.getName() + "' removed from publication: " + publication.getTitle(),
                "Publication",
                publicationId,
                performedBy
        );
    }


    public boolean canEdit(Publication publication, User user) {

        if (publication.getAuthor().getUsername().equals(user.getUsername())) {
            return true;
        }

        return user.getRole() == Role.RESPONSAVEL || user.getRole() == Role.ADMINISTRADOR;
    }

    public boolean canDelete(Publication publication, User user) {
        // Only RESPONSAVEL and ADMINISTRADOR can delete or the submitter
        if (publication.getAuthor().getUsername().equals(user.getUsername())) {
            return true;
        }
        //return false;
        return user.getRole() == Role.RESPONSAVEL || user.getRole() == Role.ADMINISTRADOR;
    }

    public boolean canView(Publication publication, User user) {
        // If visible, anyone can view
        if (publication.isVisible()) {
            return true;
        }

        // If not visible, only submitter, RESPONSAVEL, and ADMINISTRADOR can view
        if (publication.getAuthor().getUsername().equals(user.getUsername())) {
            return true;
        }

        return user.getRole() == Role.RESPONSAVEL || user.getRole() == Role.ADMINISTRADOR;
    }

    public void updateDescription(Long id, String newDescription, User performedBy) throws MyEntityNotFoundException {
        Publication publication = find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + id);
        }

        publication.setDescription(newDescription);

        // Log activity
        historyBean.logActivity(
                ActivityType.PUBLICATION_UPDATED,
                "Summary regenerated for publication: " + publication.getTitle(),
                "Publication",
                id,
                performedBy
        );
    }

    @Asynchronous
    public void generateSummaryAsync(Long publicationId, byte[] fileBytes,
                                              FileType fileType, String submitterUsername) {
        try {
            String resumoAI = null;
            String textoDocumento = null;

            // Extrair texto de ficheiros
            if (fileType == FileType.PDF) {
                InputStream streamForAI = new ByteArrayInputStream(fileBytes);
                textoDocumento = PdfTextExtractor.extractText(streamForAI);

            } else if (fileType == FileType.ZIP) {
                InputStream streamForZip = new ByteArrayInputStream(fileBytes);
                textoDocumento = ZipTextExtractor.extractTextFromPDFs(streamForZip);
            }

            // Gerar resumo
            if (textoDocumento != null && !textoDocumento.trim().isEmpty()) {
                resumoAI = aiService.generateSummary(textoDocumento);
            }

            // Atualizar a publicação com o resumo gerado
            Publication publication = find(publicationId);
            if (publication != null) {
                if (resumoAI != null && !resumoAI.trim().isEmpty()) {
                    publication.setDescription(resumoAI);
                } else {
                    publication.setDescription("Resumo automático não disponível - Por favor adicione uma descrição manualmente");
                    logger.warning("Não foi possível gerar resumo para publicação ID: " + publicationId);
                }

                em.merge(publication);

                User submitter = userBean.find(submitterUsername);
                if (submitter != null) {
                    historyBean.logActivity(
                            ActivityType.PUBLICATION_UPDATED,
                            "AI summary generated for publication: " + publication.getTitle(),
                            "Publication",
                            publicationId,
                            submitter
                    );
                }
            }

        } catch (Exception e) {
            logger.severe("Erro ao gerar resumo assíncrono para publicação ID " + publicationId + ": " + e.getMessage());
            e.printStackTrace();

            // Atualizar com mensagem de erro
            try {
                Publication publication = find(publicationId);
                if (publication != null) {
                    publication.setDescription("Erro ao gerar resumo automático. Por favor, adicione uma descrição manualmente.");
                    em.merge(publication);
                }
            } catch (Exception ex) {
                logger.severe("Erro ao atualizar publicação com mensagem de erro: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
