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
        query = "SELECT h FROM History h"
    ),
    @NamedQuery(
        name = "getHistoryLogsByUser",
        query = "SELECT h FROM History h WHERE h.user.username = :username"
    ),
        @NamedQuery(
                name = "getHistoryLogsByEntity",
                query = "SELECT h FROM History h WHERE h.entity = :entity AND h.entityId = :entityId ORDER BY h.entity DESC"
        )
})
public class History implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

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
        this.activityType = activityType;
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

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
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
