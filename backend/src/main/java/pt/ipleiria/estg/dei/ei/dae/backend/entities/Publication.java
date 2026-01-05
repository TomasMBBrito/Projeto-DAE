package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publications")
@NamedQueries({
        @NamedQuery(
                name = "getAllPublications",
                query = "SELECT p FROM Publication p ORDER BY p.scientificArea DESC"
        )
})
public class Publication implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private ScientificArea scientificArea;

    private String filePath;

    @NotBlank
    @Column(nullable = false)
    private String fileName;

    @NotBlank
    @Column(nullable = false)
    private FileType fileType;

    @NotBlank
    @Column(nullable = false)
    private LocalDate publicationDate;

    @NotBlank
    @Column(nullable = false)
    private boolean visible;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL)
    private List<Rating> ratings;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "submitter_username", nullable = false)
    private User author;

    @ManyToMany
    @JoinTable(
            name = "publication_tags",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL)
    private List<Comment> comments;

    public Publication() {
        this.tags = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public Publication(String title, String description, ScientificArea scientificArea, String fileName, FileType fileType, LocalDate publicationDate, User author, List<Tag> tags) {
        this.title = title;
        this.description = description;
        this.scientificArea = scientificArea;
        this.fileName = fileName;
        this.fileType = fileType;
        this.publicationDate = publicationDate;
        this.visible = true;
        this.author = author;
    }

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            tag.getPublications().add(this);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPublications().remove(this);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPublication(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPublication(null);
    }

    public void addRating(Rating rating) {
        ratings.add(rating);
        rating.setPublication(this);
    }

    public Double getAverageRating() {
        if (ratings.isEmpty()) {
            return null;
        }
        return ratings.stream()
                .mapToInt(Rating::getValue)
                .average()
                .orElse(0.0);
    }

    public int getRatingCount() {
        return ratings.size();
    }
}
