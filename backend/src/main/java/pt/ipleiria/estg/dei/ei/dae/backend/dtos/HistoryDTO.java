package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.ActivityType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.History;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryDTO implements Serializable {
    private String action;
    private String description;
    private String user;
    private String timestamp;

    public HistoryDTO() {
    }

    public HistoryDTO(String action, String description, String timestamp) {
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public HistoryDTO(String action, String description, String timestamp, String user) {
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static HistoryDTO from(History history) {
        return new HistoryDTO(
                history.getAction().toString(),
                history.getDescription(),
                history.getTimestamp().toString()
        );
    }

    public static HistoryDTO fromWithUser(History history) {
        return new HistoryDTO(
                history.getAction().toString(),
                history.getDescription(),
                history.getTimestamp().toString(),
                history.getUser() != null ? history.getUser().getUsername() : null
        );
    }

    public static List<HistoryDTO> from(List<History> histories) {
        return histories.stream()
                .map(HistoryDTO::from)
                .collect(Collectors.toList());
    }

    public static List<HistoryDTO> fromWithUser(List<History> histories) {
        return histories.stream()
                .map(HistoryDTO::fromWithUser)
                .collect(Collectors.toList());
    }
}