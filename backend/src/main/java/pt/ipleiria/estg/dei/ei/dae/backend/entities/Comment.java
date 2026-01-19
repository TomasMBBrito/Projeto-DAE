package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@NamedQueries({
        @NamedQuery(
                name = "getCommentsByPublication",
                query = "SELECT c FROM Comment c WHERE c.publication.id = :publicationId AND c.visible = true ORDER BY c.createdAt DESC"
        ),
        @NamedQuery(
                name = "getAllCommentsByPublication",
                query = "SELECT c FROM Comment c WHERE c.publication.id = :publicationId ORDER BY c.createdAt DESC"
        )
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String text;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;


    @Column(nullable = false)
    private boolean visible;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(String text, User user, Publication publication) {
        this.text = text;
        this.user = user;
        this.publication = publication;
        this.visible = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
