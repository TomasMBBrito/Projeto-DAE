package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "history_logs")
@NamedQueries({
    @NamedQuery(
        name = "getAllHistoryLogs",
        query = "SELECT h FROM History h ORDER BY h.timestamp DESC"
    ),
    @NamedQuery(
        name = "getHistoryLogsByUser",
        query = "SELECT h FROM History h WHERE h.user.username = :username ORDER BY h.timestamp DESC"
    ),
    @NamedQuery(
            name = "getPublicationActivities",
            query = "SELECT h FROM History h WHERE " +
                    "(h.entity = 'Publication' AND h.entityId = :publicationId) OR " +
                    "(h.entity = 'Comment' AND h.entityId IN " +
                    "  (SELECT c.id FROM Comment c WHERE c.publication.id = :publicationId)) OR " +
                    "(h.entity = 'Rating' AND h.entityId IN " +
                    "  (SELECT r.id FROM Rating r WHERE r.publication.id = :publicationId)) " +
                    "ORDER BY h.timestamp DESC"
    )
})
public class History implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType action;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String entity;


    @Column(name = "entity_id")
    private Long entityId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public History() {
    }

    public History(ActivityType activityType, String description, String entity, Long entityId, User user) {
        this.action = activityType;
        this.description = description;
        this.entity = entity;
        this.entityId = entityId;
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActivityType getAction() {
        return action;
    }

    public void setAction(ActivityType action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
