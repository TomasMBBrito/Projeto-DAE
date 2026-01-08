package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import java.util.List;

@Stateless
public class CommentBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;

    @EJB
    private EmailBean emailBean;

    public Comment create(String text, User user, Publication publication) {
        Comment comment = new Comment(text, user, publication);
        em.persist(comment);

        publication.addComment(comment);

        historyBean.logActivity(
                ActivityType.COMMENT_CREATED,
                "Comment added to publication: " + publication.getTitle(),
                "Comment",
                comment.getId(),
                user
        );

        // Notify subscribers of tags on this publication
        for (Tag tag : publication.getTags()) {
            emailBean.notifyTagSubscribers(
                    tag,
                    publication,
                    "New comment on publication with tag: " + tag.getName()
            );
        }

        return comment;
    }

    public Comment find(Long id) {
        return em.find(Comment.class, id);
    }

    public List<Comment> getByPublication(Long publicationId, boolean includeHidden) {
        if (includeHidden) {
            return em.createNamedQuery("getAllCommentsByPublication", Comment.class)
                    .setParameter("publicationId", publicationId)
                    .getResultList();
        } else {
            return em.createNamedQuery("getCommentsByPublication", Comment.class)
                    .setParameter("publicationId", publicationId)
                    .getResultList();
        }
    }


    public void update(Long id, String text, User performedBy) {
        Comment comment = find(id);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + id);
        }

        comment.setText(text);

        historyBean.logActivity(
                ActivityType.COMMENT_UPDATED,
                "Comment updated",
                "Comment",
                id,
                performedBy
        );
    }

    public void hide(Long id, User performedBy) {
        Comment comment = find(id);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + id);
        }

        comment.setVisible(false);

        historyBean.logActivity(
                ActivityType.COMMENT_HIDDEN,
                "Comment hidden",
                "Comment",
                id,
                performedBy
        );
    }

    public void show(Long id, User performedBy) {
        Comment comment = find(id);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + id);
        }

        comment.setVisible(true);

        historyBean.logActivity(
                ActivityType.COMMENT_SHOWN,
                "Comment shown",
                "Comment",
                id,
                performedBy
        );
    }

    // DELETE
    public void delete(Long id, User performedBy) {
        Comment comment = find(id);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found: " + id);
        }

        historyBean.logActivity(
                ActivityType.COMMENT_DELETED,
                "Comment deleted",
                "Comment",
                id,
                performedBy
        );

        em.remove(comment);
    }

    // PERMISSIONS
    public boolean canEdit(Comment comment, User user) {
        // Only the comment author can edit
        return comment.getUser().getUsername().equals(user.getUsername());
    }

    public boolean canDelete(Comment comment, User user) {
        // Comment author, RESPONSAVEL, or ADMINISTRADOR can delete
        if (comment.getUser().getUsername().equals(user.getUsername())) {
            return true;
        }
        return user.getRole() == Role.RESPONSAVEL || user.getRole() == Role.ADMINISTRADOR;
    }
}
