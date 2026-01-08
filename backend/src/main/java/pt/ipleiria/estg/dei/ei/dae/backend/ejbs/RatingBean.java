package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;

@Stateless
public class RatingBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;


    public Rating createOrUpdate(int value, User user, Publication publication) {
        Rating existing = findByUserAndPublication(user.getUsername(), publication.getId());

        if (existing != null) {
            // Update existing rating
            existing.setValue(value);

            historyBean.logActivity(
                    ActivityType.RATING_UPDATED,
                    "Rating updated to " + value + " stars for: " + publication.getTitle(),
                    "Rating",
                    existing.getId(),
                    user
            );

            return existing;
        } else {
            // Create new rating
            Rating rating = new Rating(value, user, publication);
            em.persist(rating);

            publication.addRating(rating);

            historyBean.logActivity(
                    ActivityType.RATING_CREATED,
                    "Rated " + value + " stars for: " + publication.getTitle(),
                    "Rating",
                    rating.getId(),
                    user
            );

            return rating;
        }
    }


    public Rating find(Long id) {
        return em.find(Rating.class, id);
    }

    public Rating findByUserAndPublication(String username, Long publicationId) {
        try {
            return em.createNamedQuery("getRatingByUserAndPublication", Rating.class)
                    .setParameter("username", username)
                    .setParameter("publicationId", publicationId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }


    public void delete(Long id, User performedBy) {
        Rating rating = find(id);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found: " + id);
        }

        historyBean.logActivity(
                ActivityType.RATING_DELETED,
                "Rating deleted",
                "Rating",
                id,
                performedBy
        );

        em.remove(rating);
    }


    public boolean canDelete(Rating rating, User user) {
        // Only the rating author can delete
        return rating.getUser().getUsername().equals(user.getUsername());
    }
}