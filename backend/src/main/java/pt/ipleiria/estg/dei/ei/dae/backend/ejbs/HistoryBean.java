package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.ActivityType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.History;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;

import java.util.List;

@Stateless
public class HistoryBean {

    @PersistenceContext
    private EntityManager em;

    public void logActivity(ActivityType activityType, String description,
                            String entity, Long entityId, User user) {
        try {
            History history = new History(activityType, description, entity, entityId, user);
            em.persist(history);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public History find(Long id) {
        return em.find(History.class, id);
    }

    public List<History> getAllHistory() {
        return em.createNamedQuery("getAllHistoryLogs", History.class).getResultList();
    }

    public List<History> getRecentHistory(int limit){
        return em.createQuery("SELECT h FROM History h ORDER BY h.timestamp DESC", History.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<History> getHistoryByType(ActivityType activityType) {
        return em.createQuery(
                        "SELECT h FROM History h WHERE h.action = :type ORDER BY h.timestamp DESC",
                        History.class)
                .setParameter("type", activityType)
                .getResultList();
    }

    public List<History> getUserHistory(String username) {
        return em.createNamedQuery("getHistoryLogsByUser", History.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<History> getPublicationHistory(Long entityId) {
        return em.createNamedQuery("getPublicationActivities", History.class)
                .setParameter("publicationId", entityId)
                .getResultList();
    }

    public List<History> getRecent(int limit) {
        return em.createQuery(
                        "SELECT h FROM History h ORDER BY h.timestamp DESC",
                        History.class
                )
                .setMaxResults(limit)
                .getResultList();
    }
}
