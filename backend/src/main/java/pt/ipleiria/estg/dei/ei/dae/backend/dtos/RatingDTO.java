package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.Rating;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class RatingDTO implements Serializable {
    private Long id;
    private int value;

    private String username;
    private String userFullName;


    private Long publicationId;

    public RatingDTO() {
    }

    public RatingDTO(Long id, int value, String username, String userFullName, Long publicationId) {
        this.id = id;
        this.value = value;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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


    public static RatingDTO from(Rating rating) {
        return new RatingDTO(
                rating.getId(),
                rating.getValue(),
                rating.getUser() != null ? rating.getUser().getUsername() : null,
                rating.getUser() != null ? rating.getUser().getName() : null,
                rating.getPublication() != null ? rating.getPublication().getId() : null
        );
    }

    public static List<RatingDTO> from(List<Rating> ratings) {
        return ratings.stream()
                .map(RatingDTO::from)
                .collect(Collectors.toList());
    }
}