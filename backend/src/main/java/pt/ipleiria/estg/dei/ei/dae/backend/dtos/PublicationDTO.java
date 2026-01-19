package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {
    private long id;
    private String title;
    private String summary;
    private ScientificArea scientificArea;
    private LocalDate publicationDate;
    private List<String> authors;
    private boolean visible;

    private String submitterUsername;
    private String lastComment;

    // Campos para criação/edição
    private String filename;
    private List<Long> tagIds;

    // Stats
    private Double averageRating;
    private int ratingCount;
    private int commentCount;

    private List<TagDTO> tags;

    public PublicationDTO() {
        this.tags = new ArrayList<>();
        this.authors = new ArrayList<>();
        this.tagIds = new ArrayList<>();
    }

    public PublicationDTO(Long id, String title, String author, String summary) {
        this();
        this.id = id;
        this.title = title;
        this.submitterUsername = author;
        this.summary = summary;
    }

    public PublicationDTO(Long id, String title, String author, String summary, boolean visible) {
        this(id, title, author, summary);
        this.visible = visible;
    }

    public PublicationDTO(Long id, String title, String author, String summary,
                          ScientificArea scientificArea) {
        this(id, title, author, summary);
        this.scientificArea = scientificArea;
    }

    public PublicationDTO(Long id, String title, String author, String summary,
                          ScientificArea scientificArea, String comment) {
        this(id, title, author, summary, scientificArea);
        this.lastComment = comment;
    }

    public PublicationDTO(Long id, String title, String author, String summary,
                          ScientificArea scientificArea, LocalDate createdAt) {
        this(id, title, author, summary, scientificArea);
        this.publicationDate = createdAt;
    }

    // Getters e Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ScientificArea getScientificArea() {
        return scientificArea;
    }

    public void setScientificArea(ScientificArea scientificArea) {
        this.scientificArea = scientificArea;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSubmitterUsername() {
        return submitterUsername;
    }

    public void setSubmitterUsername(String submitterUsername) {
        this.submitterUsername = submitterUsername;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    // Métodos estáticos para conversão
    public static PublicationDTO FromCollaborator(Publication pub) {
        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription()
        );
    }

    public static PublicationDTO FromAdminOrRespList(Publication pub) {
        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription(),
                pub.isVisible()
        );
    }

    public static PublicationDTO FromDetails(Publication pub) {
        PublicationDTO dto = new PublicationDTO();
        dto.setId(pub.getId());
        dto.setTitle(pub.getTitle());
        dto.setSubmitterUsername(pub.getAuthor() != null ? pub.getAuthor().getUsername() : null);
        dto.setSummary(pub.getDescription());
        dto.setPublicationDate(pub.getPublicationDate());
        dto.setScientificArea(pub.getScientificArea());
        dto.setVisible(pub.isVisible());

        if (pub.getTags() != null) {
            dto.setTags(pub.getTags().stream()
                    .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static PublicationDTO FromSearch(Publication pub) {
        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription(),
                pub.getScientificArea()
        );
    }

    public static PublicationDTO FromSearchWithComment(Publication pub) {
        String lastComment = null;
        if (pub.getComments() != null && !pub.getComments().isEmpty()) {
            // Comentário mais recente
            lastComment = pub.getComments().stream()
                    .filter(Comment::isVisible) // Apenas comentários visíveis
                    .max(Comparator.comparing(Comment::getCreatedAt))
                    .map(Comment::getText)
                    .orElse(null);
        }

        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription(),
                pub.getScientificArea(),
                lastComment
        );
    }

    public static PublicationDTO FromSort(Publication pub) {
        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription(),
                pub.getScientificArea(),
                pub.getPublicationDate()
        );
    }

    public static PublicationDTO fromSimple(Publication pub) {
        return new PublicationDTO(
                pub.getId(),
                pub.getTitle(),
                pub.getAuthor() != null ? pub.getAuthor().getUsername() : null,
                pub.getDescription(),
                pub.isVisible()
        );
    }

    // Métodos para listas
    public static List<PublicationDTO> forCollaboratorList(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::FromCollaborator)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> forAdminList(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::FromAdminOrRespList)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> forSearch(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::FromSearch)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> forSearchWithComments(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::FromSearchWithComment)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> forSort(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::FromSort)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> fromSimple(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::fromSimple)
                .collect(Collectors.toList());
    }

    public static List<PublicationDTO> from(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::fromSimple)
                .collect(Collectors.toList());
    }
}