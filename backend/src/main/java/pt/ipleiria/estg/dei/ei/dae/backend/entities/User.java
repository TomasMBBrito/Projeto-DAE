package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public class User {
    @Id
    private String username;

    @NotBlank
    private String password;

    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private Role role;

    @OneToMany(mappedBy = "submitter",cascade = CascadeType.ALL)
    private List<Publication> posts;

    @ManyToMany
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
}
