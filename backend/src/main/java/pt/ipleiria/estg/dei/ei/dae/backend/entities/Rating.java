package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "publication_id"})
)
public class Rating implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private int value;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    public Rating() {
    }

    public Rating(int value, User user, Publication publication) {
        this.value = value;
        this.user = user;
        this.publication = publication;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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
}
