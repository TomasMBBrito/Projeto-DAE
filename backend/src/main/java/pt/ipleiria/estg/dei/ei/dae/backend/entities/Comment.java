package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "comments")
@NamedQueries({
        @NamedQuery(
                name = "getCommentsByPublication",
                query = "SELECT c FROM Comment c WHERE c.publication.id = :publicationId AND c.visible = true"
        )
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String text;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    @NotBlank
    @Column(nullable = false)
    private boolean visible;

    public Comment() {
    }

    public Comment(String text, User user, Publication publication) {
        this.text = text;
        this.user = user;
        this.publication = publication;
        this.visible = true;
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
}
