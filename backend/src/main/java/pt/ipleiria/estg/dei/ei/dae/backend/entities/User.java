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
        ),
        @NamedQuery(
                name = "getAllVisibleUsers",
                query = "SELECT u FROM User u WHERE u.blocked= FALSE ORDER BY u.username ASC"
        ),

})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

public class User extends Versionable implements Serializable {
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
    private boolean blocked = false;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    private List<Publication> posts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_tag_subscriptions",
            joinColumns = @JoinColumn(name = "user_username"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> subscribedTags;

    public User() {
        this.posts = new ArrayList<>();
        this.subscribedTags = new ArrayList<>();
    }

    public User(String username, String password, String email, String name, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
        this.blocked = false;
        this.posts = new ArrayList<>();
        this.subscribedTags = new ArrayList<>();
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

    public List<Tag> getSubscribedTags() {
        return subscribedTags;
    }

    public void setSubscribedTags(List<Tag> subscribedTags) {
        this.subscribedTags = subscribedTags;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void addPublication(Publication publication) {
        if (!posts.contains(publication)) {
            posts.add(publication);
        }
    }

    public void subscribeTag(Tag tag) {
        if (!subscribedTags.contains(tag)) {
            subscribedTags.add(tag);
            tag.getSubscribers().add(this);
        }else{
            throw new IllegalArgumentException("Já está subscrito a esta tag");
        }
    }

    public void unsubscribeTag(Tag tag) {
        if(!subscribedTags.contains(tag)){
            throw new IllegalArgumentException("Não está subscrito a esta tag");
        }
        subscribedTags.remove(tag);
        tag.getSubscribers().remove(this);
    }
}
