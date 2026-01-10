package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class UserDTO implements Serializable {
    private String username;
    private String email;
    private String password;
    private String name;
    private Role role;
    private boolean active;
    private List<String> subscribedTags;
    //private List<String> publications;

    public UserDTO() {
        this.subscribedTags = new ArrayList<>();
        //this.publications = new ArrayList<>();
    }

    public UserDTO(String username, String email, String name, Role role, boolean active) {
        this.username = username;
        this.email = email;
        //this.password = password;
        this.name = name;
        this.role = role;
        this.active = active;
        this.subscribedTags = new ArrayList<>();
        //this.publications = new ArrayList<>();
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getSubscribedTags() {
        return subscribedTags;
    }

    public void setSubscribedTags(List<String> subscribedTags) {
        this.subscribedTags = subscribedTags;
    }

    public static UserDTO from(User user) {
        UserDTO dto = new UserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isActive()
        );

        if (user.getSubscribed_tags() != null) {
            dto.setSubscribedTags(
                    user.getSubscribed_tags().stream()
                            .map(tag -> tag.getName())
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    public static List<UserDTO> from(List<User> users) {
        return users.stream().map(UserDTO::from).collect(Collectors.toList());
    }


}