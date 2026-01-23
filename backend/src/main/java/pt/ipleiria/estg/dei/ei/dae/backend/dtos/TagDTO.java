package pt.ipleiria.estg.dei.ei.dae.backend.dtos;

import pt.ipleiria.estg.dei.ei.dae.backend.entities.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class TagDTO implements Serializable {
    private Long id;
    private String name;
    private boolean visible;


    private int publicationCount;
    private int subscriberCount;

    public TagDTO() {
    }

    public TagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TagDTO(Long id, String name, boolean visible) {
        this.id = id;
        this.name = name;
        this.visible = visible;
    }

    public TagDTO(Long id, String name, boolean visible, int publicationCount, int subscriberCount) {
        this.id = id;
        this.name = name;
        this.visible = visible;
        this.publicationCount = publicationCount;
        this.subscriberCount = subscriberCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(int publicationCount) {
        this.publicationCount = publicationCount;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public static TagDTO fromSimple(Tag tag) {
        return new TagDTO(tag.getId(), tag.getName());
    }


    public static TagDTO from(Tag tag) {
        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.isVisible(),
                tag.getPublications() != null ? tag.getPublications().size() : 0,
                tag.getSubscribers() != null ? tag.getSubscribers().size() : 0
        );
    }

    public static List<TagDTO> fromSimple(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::fromSimple)
                .collect(Collectors.toList());
    }

    public static List<TagDTO> from(List<Tag> tags) {
        return tags.stream()
                .map(TagDTO::from)
                .collect(Collectors.toList());
    }
}