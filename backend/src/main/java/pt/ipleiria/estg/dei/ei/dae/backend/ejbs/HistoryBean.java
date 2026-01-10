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
            // Don't let logging errors break the main operation
            e.printStackTrace();
        }
    }


    public History find(Long id) {
        return em.find(History.class, id);
    }

    public List<History> getAll() {
        return em.createNamedQuery("getAllHistoryLogs", History.class).getResultList();
    }

    public List<History> getByUser(String username) {
        return em.createNamedQuery("getHistoryLogsByUser", History.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<History> getByEntity(String entity, Long entityId) {
        return em.createNamedQuery("getHistoryLogsByEntity", History.class)
                .setParameter("entity", entity)
                .setParameter("entityId", entityId)
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
