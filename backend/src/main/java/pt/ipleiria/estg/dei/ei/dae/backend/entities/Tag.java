package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@NamedQueries({
        @NamedQuery(
                name = "getAllTags",
                query = "SELECT t FROM Tag t WHERE t.visible = true ORDER BY t.name"
        ),
        @NamedQuery(
                name = "getTagByName",
                query = "SELECT t FROM Tag t WHERE t.name = :name"
        )
})
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private boolean visible;

    @ManyToMany(mappedBy = "tags")
    private List<Publication> publications;

    @ManyToMany(mappedBy = "subscribedTags")
    private List<User> subscribers;

    public Tag() {
        this.publications = new ArrayList<>();
        this.subscribers = new ArrayList<>();
    }

    public Tag(String name) {
        this.name = name;
        this.visible = true;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<User> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<User> subscribers) {
        this.subscribers = subscribers;
    }
}
