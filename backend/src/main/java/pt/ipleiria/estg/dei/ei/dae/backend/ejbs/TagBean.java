package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;

import java.util.List;

@Stateless
public class TagBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;

    @EJB
    private UserBean userbean;


    public Tag create(String name, User performedBy) {
        Tag tag = new Tag(name);
        em.persist(tag);

        historyBean.logActivity(
                ActivityType.TAG_CREATED,
                "Tag created: " + name,
                "Tag",
                tag.getId(),
                performedBy
        );

        return tag;
    }

    public Tag update(Long id, String newName,String username) throws MyEntityNotFoundException {
        Tag tag = find(id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found: " + id);
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new MyEntityNotFoundException("Tag name cannot be empty");
        }

        em.lock(tag, LockModeType.OPTIMISTIC);

        tag.setName(newName);

        User performedBy = userbean.find(username);
        if(performedBy == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        historyBean.logActivity(
                ActivityType.TAG_UPDATED,
                "Tag updated: " + newName,
                "Tag",
                id,
                performedBy
        );

        // Persist the change
        em.merge(tag);

        return tag;
    }



    public Tag find(Long id) {
        return em.find(Tag.class, id);
    }

    public Tag findWithDetails(Long id) {
        Tag tag = find(id);
        if (tag != null) {
            Hibernate.initialize(tag.getPublications());
            Hibernate.initialize(tag.getSubscribers());
        }
        return tag;
    }

    public Tag findByName(String name) {
        try {
            return em.createNamedQuery("getTagByName", Tag.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Tag findWithPublications(Long id) {
        Tag tag = find(id);
        if (tag != null) {
            Hibernate.initialize(tag.getPublications());
        }
        return tag;
    }

    public Tag findWithSubscribers(Long id) {
        Tag tag = find(id);
        if (tag != null) {
            Hibernate.initialize(tag.getSubscribers());
        }
        return tag;
    }

    public List<Tag> getSubscribedTags(String username)
            throws MyEntityNotFoundException {

        User user = userbean.find(username);

        Hibernate.initialize(user.getSubscribedTags());
        return user.getSubscribedTags();
    }

    public List<Tag> getAllVisible() {
        return em.createNamedQuery("getTags", Tag.class).getResultList();
    }

    public List<Tag> getAll() {
        return em.createNamedQuery("getAllTags", Tag.class).getResultList();
    }


    public void hide(Long id, String username) throws MyEntityNotFoundException {
        Tag tag = find(id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found: " + id);
        }

        tag.setVisible(false);

        User performedBy = userbean.find(username);
        if(performedBy == null) {
            throw new MyEntityNotFoundException("User not found " + username);
        }

        historyBean.logActivity(
                ActivityType.TAG_DELETED,
                "Tag hidden: " + tag.getName(),
                "Tag",
                id,
                performedBy
        );
    }

    public void show(Long id, String username) throws MyEntityNotFoundException {
        Tag tag = find(id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found: " + id);
        }

        tag.setVisible(true);

        User performedBy = userbean.find(username);
        if(performedBy == null) {
            throw new MyEntityNotFoundException("User not found " + username);
        }

        historyBean.logActivity(
                ActivityType.TAG_CREATED,
                "Tag shown: " + tag.getName(),
                "Tag",
                id,
                performedBy
        );
    }


    public void delete(Long id,String username) throws MyEntityNotFoundException {
        Tag tag = find(id);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found: " + id);
        }

        String name = tag.getName();

        User performedBy = userbean.find(username);
        if(performedBy == null) {
            throw new MyEntityNotFoundException("User not found " + username);
        }

        historyBean.logActivity(
                ActivityType.TAG_DELETED,
                "Tag deleted: " + name,
                "Tag",
                id,
                performedBy
        );

        em.remove(tag);
    }

    public boolean exists(String name) {
        return findByName(name) != null;
    }
}