package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;

@Stateless
public class RatingBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;


    public Rating createOrUpdate(int value, User user, Long publicationId) throws MyEntityNotFoundException {

        Publication publication = em.find(Publication.class, publicationId);

        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found: " + publicationId);
        }

        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("Rating deve estar entre 1 e 5 estrelas");
        }


        Rating existing = findByUserAndPublication(user.getUsername(), publication.getId());



        if (existing != null) {
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


    public void delete(Long id, User performedBy) throws MyEntityNotFoundException {
        Rating rating = find(id);
        if (rating == null) {
            throw new MyEntityNotFoundException("Rating not found: " + id);
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
        return rating.getUser().getUsername().equals(user.getUsername());
    }
}