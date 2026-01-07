package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.ActivityType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.History;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryDTO implements Serializable {
    private Long id;
    private ActivityType activityType;
    private String description;
    private String entity;
    private Long entityId;


    private String username;
    private String userFullName;

    private LocalDateTime timestamp;

    public HistoryDTO() {
    }

    public HistoryDTO(Long id, ActivityType activityType, String description,
                      String entity, Long entityId, String username,
                      String userFullName, LocalDateTime timestamp) {
        this.id = id;
        this.activityType = activityType;
        this.description = description;
        this.entity = entity;
        this.entityId = entityId;
        this.username = username;
        this.userFullName = userFullName;
        this.timestamp = timestamp;
    }

    // Getters and Setters
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public static HistoryDTO from(History history) {
        return new HistoryDTO(
                history.getId(),
                history.getActivityType(),
                history.getDescription(),
                history.getEntity(),
                history.getEntityId(),
                history.getUser() != null ? history.getUser().getUsername() : null,
                history.getUser() != null ? history.getUser().getName() : null,
                history.getTimestamp()
        );
    }

    public static List<HistoryDTO> from(List<History> histories) {
        return histories.stream()
                .map(HistoryDTO::from)
                .collect(Collectors.toList());
    }
}