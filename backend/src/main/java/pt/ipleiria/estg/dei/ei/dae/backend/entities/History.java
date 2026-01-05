package pt.ipleiria.estg.dei.ei.dae.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.usertype.UserType;

import java.io.Serializable;

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
    )
})
public class History implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String entity;

    @NotBlank
    @Column(name = "entity_id")
    private Long entityId;

    @NotBlank
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    public History() {
    }

    public History(ActivityType activityType, String description, String entity, Long entityId, User user) {
        this.activityType = activityType;
        this.description = description;
        this.entity = entity;
        this.entityId = entityId;
        this.user = user;
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
}
