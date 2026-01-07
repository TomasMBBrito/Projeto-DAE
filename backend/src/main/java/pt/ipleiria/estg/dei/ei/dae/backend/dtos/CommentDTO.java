package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.Comment;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO implements Serializable {
    private Long id;
    private String text;
    private boolean visible;


    private String username;
    private String userFullName;


    private Long publicationId;

    public CommentDTO() {
    }

    public CommentDTO(Long id, String text, boolean visible, String username,
                      String userFullName, Long publicationId) {
        this.id = id;
        this.text = text;
        this.visible = visible;
        this.username = username;
        this.userFullName = userFullName;
        this.publicationId = publicationId;
    }

    // Getters and Setters
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    // Static factory method - converts Entity to DTO
    public static CommentDTO from(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                comment.isVisible(),
                comment.getUser() != null ? comment.getUser().getUsername() : null,
                comment.getUser() != null ? comment.getUser().getName() : null,
                comment.getPublication() != null ? comment.getPublication().getId() : null
        );
    }

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream()
                .map(CommentDTO::from)
                .collect(Collectors.toList());
    }
}