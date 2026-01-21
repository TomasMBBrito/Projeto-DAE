package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.ActivityType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyConstraintViolationException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Hasher;

import java.util.List;

@Stateless
public class UserBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;

    @EJB
    private TagBean tagBean;

    public User create(String username, String password, String email, String name, Role role,User user_performing) throws MyEntityExistsException, MyConstraintViolationException {
        if(em.find(User.class, username) != null) {
            throw new MyEntityExistsException("User already exists: " + username);
        }
        try {
            User user = new User(username, Hasher.hash(password), email, name, role);
            em.persist(user);
            em.flush();
            // Log activity
            if (user_performing != null) {
                historyBean.logActivity(
                        ActivityType.USER_CREATED,
                        "User created: " + username,
                        "User",
                        null,
                        user_performing
                );
            }

            return user;
        }catch (ConstraintViolationException e){
            throw new MyConstraintViolationException(e);
        }
    }

    public User find(String username) throws MyEntityNotFoundException {
        User user = em.find(User.class, username);
        if(user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }
        return user;
    }

    public User findByMail(String email) throws MyEntityNotFoundException {
        User user = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email",
                        User.class
                )
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return user;
    }

    public List<User> getAll(User user) {
        if(user.getRole().equals(Role.ADMINISTRADOR)) {
            return em.createNamedQuery("getAllUsers", User.class).getResultList();
        }
        return em.createNamedQuery("getAllVisibleUsers", User.class).getResultList();
    }

    public void update(String username, String email, String name) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }
        em.lock(user, LockModeType.OPTIMISTIC);
        user.setEmail(email);
        user.setName(name);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_UPDATED,
                "User updated: " + username,
                "User",
                null,
                user
        );
    }

    public void changePassword(String username,String oldPassword, String newPassword) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        if (!Hasher.verify(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password antiga incorreta");
        }

        user.setPassword(Hasher.hash(newPassword));
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_UPDATED,
                "Password changed for user: " + username,
                "User",
                null,
                user
        );
    }

    public void resetPassword(String username, String newPassword) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        if(newPassword == null || newPassword.trim().isEmpty()){
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        user.setPassword(Hasher.hash(newPassword));
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_PASSWORD_RESET,
                "Password reset for user: " + username,
                "User",
                null,
                user
        );
    }

    public void changeRole(String username, Role newRole, User performedBy) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        Role oldRole = user.getRole();
        user.setRole(newRole);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_ROLE_CHANGED,
                "User role changed from " + oldRole + " to " + newRole + " for: " + username,
                "User",
                null,
                performedBy
        );
    }

    public void activate(String username, User performedBy) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        user.setBlocked(false);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_ACTIVATED,
                "User activated: " + username,
                "User",
                null,
                performedBy
        );
    }

    public void deactivate(String username, User performedBy) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        user.setBlocked(true);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_DEACTIVATED,
                "User deactivated: " + username,
                "User",
                null,
                performedBy
        );
    }

    public void delete(String username, User performedBy) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        // Log activity BEFORE deletion
        historyBean.logActivity(
                ActivityType.USER_DELETED,
                "User deleted: " + username,
                "User",
                null,
                performedBy
        );

        em.remove(user);
    }

    public void subscribeTag(String username, Long tagId) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        Tag tag = tagBean.find(tagId);
        if(tag == null){
            throw new MyEntityNotFoundException("Tag not found with id");
        }

        user.subscribeTag(tag);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_SUBSCRIBED,
                "User " + username + " subscribed to tag: " + tag.getName(),
                "Tag",
                tag.getId(),
                user
        );
    }

    public void unsubscribeTag(String username,Long tagId) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }

        Tag tag = tagBean.find(tagId);
        if(tag == null){
            throw new MyEntityNotFoundException("Tag not found with id");
        }

        user.unsubscribeTag(tag);
        em.merge(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_UNSUBSCRIBED,
                "User " + username + " unsubscribed from tag: " + tag.getName(),
                "Tag",
                tag.getId(),
                user
        );
    }

    public boolean hasActivity(String username) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }


        Long publicationCount = em.createQuery(
                "SELECT COUNT(p) FROM Publication p WHERE p.author.username = :username",
                Long.class
        ).setParameter("username", username).getSingleResult();


        Long commentCount = em.createQuery(
                "SELECT COUNT(c) FROM Comment c WHERE c.user.username = :username",
                Long.class
        ).setParameter("username", username).getSingleResult();


        Long ratingCount = em.createQuery(
                "SELECT COUNT(r) FROM Rating r WHERE r.user.username = :username",
                Long.class
        ).setParameter("username", username).getSingleResult();


        Long historyCount = em.createQuery(
                "SELECT COUNT(h) FROM History h WHERE h.user.username = :username",
                Long.class
        ).setParameter("username", username).getSingleResult();


        return (publicationCount > 0 || commentCount > 0 || ratingCount > 0 || historyCount > 0);
    }

    public boolean exists(String username){
        try {
            find(username);
            return true;
        } catch (MyEntityNotFoundException e) {
            return false;
        }
    }

    public boolean canLogin(String username, String password) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null || user.isBlocked()) {
            return false;
        }

        return Hasher.verify(password, user.getPassword());
    }
}
