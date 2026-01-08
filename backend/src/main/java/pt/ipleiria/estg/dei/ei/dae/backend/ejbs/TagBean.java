package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;

import java.util.List;

@Stateless
public class TagBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;


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


    public Tag find(Long id) {
        return em.find(Tag.class, id);
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

    public List<Tag> getAllVisible() {
        return em.createNamedQuery("getTags", Tag.class).getResultList();
    }

    public List<Tag> getAll() {
        return em.createNamedQuery("getAllTags", Tag.class).getResultList();
    }


    public void hide(Long id, User performedBy) {
        Tag tag = find(id);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found: " + id);
        }

        tag.setVisible(false);

        historyBean.logActivity(
                ActivityType.TAG_DELETED,
                "Tag hidden: " + tag.getName(),
                "Tag",
                id,
                performedBy
        );
    }

    public void show(Long id, User performedBy) {
        Tag tag = find(id);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found: " + id);
        }

        tag.setVisible(true);

        historyBean.logActivity(
                ActivityType.TAG_CREATED,
                "Tag shown: " + tag.getName(),
                "Tag",
                id,
                performedBy
        );
    }


    public void delete(Long id, User performedBy) {
        Tag tag = find(id);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found: " + id);
        }

        String name = tag.getName();

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