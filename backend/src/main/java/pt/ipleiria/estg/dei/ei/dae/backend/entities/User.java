package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(
                name = "getAllUsers",
                query = "SELECT u FROM User u ORDER BY u.username ASC"
        )
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

public class User implements Serializable {
    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    private List<Publication> posts;

    @ManyToMany
    @JoinTable(
            name = "user_tag_subscriptions",
            joinColumns = @JoinColumn(name = "user_username"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> subscribed_tags;

    public User() {
        this.posts = new ArrayList<>();
        this.subscribed_tags = new ArrayList<>();
    }

    public User(String username, String password, String email, String name, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
        this.active = true;
        this.posts = new ArrayList<>();
        this.subscribed_tags = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Publication> getPosts() {
        return posts;
    }

    public void setPosts(List<Publication> posts) {
        this.posts = posts;
    }

    public List<Tag> getSubscribed_tags() {
        return subscribed_tags;
    }

    public void setSubscribed_tags(List<Tag> subscribed_tags) {
        this.subscribed_tags = subscribed_tags;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addPublication(Publication publication) {
        if (!posts.contains(publication)) {
            posts.add(publication);
        }
    }

    public void subscribeTag(Tag tag) {
        if (!subscribed_tags.contains(tag)) {
            subscribed_tags.add(tag);
            tag.getSubscribers().add(this);
        }
    }

    public void unsubscribeTag(Tag tag) {
        subscribed_tags.remove(tag);
        tag.getSubscribers().remove(this);
    }
}
