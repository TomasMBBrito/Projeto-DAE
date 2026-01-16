package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.FileType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.ScientificArea;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO implements Serializable {
    private long id;
    private String title;
    private String description;
    private ScientificArea scientificArea;
//    private String fileName;
//    private FileType fileType;
    private LocalDate publicationDate;
    private List<String> authors;
    private boolean visible;

    private String submitterUsername;

    // Stats
    private Double averageRating;
    private int ratingCount;
    private int commentCount;

    private List<String> tags;

    public PublicationDTO() {
        this.tags = new ArrayList<>();
    }

    public PublicationDTO(Long id, String title, String author, boolean visible) {
        this.id = id;
        this.title = title;
        this.submitterUsername = author;
        this.visible = visible;
    }

    public PublicationDTO(long id, String title, String description, ScientificArea scientificArea,
                          LocalDate publicationDate,
                          List<String> authors, boolean visible) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scientificArea = scientificArea;
//        this.fileName = fileName;
//        this.fileType = fileType;
        this.publicationDate = publicationDate;
        this.authors = authors;
        this.visible = visible;
        this.tags = new ArrayList<>();
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScientificArea getScientificArea() {
        return scientificArea;
    }

    public void setScientificArea(ScientificArea scientificArea) {
        this.scientificArea = scientificArea;
    }

//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public FileType getFileType() {
//        return fileType;
//    }
//
//    public void setFileType(FileType fileType) {
//        this.fileType = fileType;
//    }

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }


    public static PublicationDTO from(Publication publication) {
        PublicationDTO dto = new PublicationDTO(
                publication.getId(),
                publication.getTitle(),
                publication.getDescription(),
                publication.getScientificArea(),
//                publication.getFileName(),
//                publication.getFileType(),
                publication.getPublicationDate(),
                publication.getAuthors(),
                publication.isVisible()
        );


        if (publication.getAuthor() != null) {
            dto.setSubmitterUsername(publication.getAuthor().getUsername());
        }

        // Stats
        dto.setAverageRating(publication.getAverageRating());
        dto.setRatingCount(publication.getRatingCount());
        dto.setCommentCount(publication.getComments() != null ? publication.getComments().size() : 0);


        if (publication.getTags() != null) {
            dto.setTags(
                    publication.getTags().stream()
                            .map(tag -> tag.getName())
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static List<PublicationDTO> from(List<Publication> publications) {
        return publications.stream()
                .map(PublicationDTO::from)
                .collect(Collectors.toList());
    }
}