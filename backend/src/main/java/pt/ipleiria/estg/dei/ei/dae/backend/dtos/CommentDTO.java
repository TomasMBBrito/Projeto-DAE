package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.Comment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO implements Serializable {
    private Long id;
    private String content;
    private boolean visible;
    private String author;
    private String createdAt;

    private Long publicationId;

    public CommentDTO() {}

    public CommentDTO(Long id, String author, String content, String createdAt) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    public CommentDTO(Long id, String author, String text, boolean visible, String createdAt) {
        this(id, author,text, createdAt);
        this.visible = visible;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(Long publicationId) {
        this.publicationId = publicationId;
    }

    public static CommentDTO forCollaborator(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUser() != null ? comment.getUser().getUsername() : null,
                comment.getText(),
                comment.getCreatedAt().toString()
        );
    }

    public static CommentDTO forAdmin(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUser() != null ? comment.getUser().getUsername() : null,
                comment.getText(),
                comment.isVisible(),
                comment.getCreatedAt().toString()
        );
    }


    public static List<CommentDTO> forCollaboratorList(List<Comment> comments) {
        return comments.stream()
                .filter(Comment::isVisible)  // Colaborador só vê visíveis
                .map(CommentDTO::forCollaborator)
                .collect(Collectors.toList());
    }

    public static List<CommentDTO> forAdminList(List<Comment> comments) {
        return comments.stream()
                .map(CommentDTO::forAdmin)
                .collect(Collectors.toList());
    }
}