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


    private String oldpassword;
    private String newpassword;


    private String name;
    private Role role;
    private boolean blocked;
    private List<String> subscribedTags;
    private List<String> publications;

    public UserDTO() {
        this.subscribedTags = new ArrayList<>();
        this.publications = new ArrayList<>();
    }

    public UserDTO(String username, String email, String name, Role role, boolean blocked) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
        this.blocked = blocked;
        this.subscribedTags = new ArrayList<>();
        this.publications = new ArrayList<>();
    }


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

    public String getOldpassword() {
        return oldpassword;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public List<String> getSubscribedTags() {
        return subscribedTags;
    }

    public void setSubscribedTags(List<String> subscribedTags) {
        this.subscribedTags = subscribedTags;
    }

    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

    public static UserDTO from(User user) {
        UserDTO dto = new UserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isBlocked()
        );

        if (user.getSubscribedTags() != null) {
            dto.setSubscribedTags(
                    user.getSubscribedTags().stream()
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