package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.ActivityType;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Role;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Hasher;

import java.util.List;

@Stateless
public class UserBean {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private HistoryBean historyBean;

    public User create(String username, String password, String email, String name, Role role) {
        User user = new User(username, password, email, name, role);
        em.persist(user);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_CREATED,
                "User created: " + username,
                "User",
                null,
                user
        );

        return user;
    }

    public User find(String username) throws MyEntityNotFoundException {
        User user = em.find(User.class, username);
        if(user == null) {
            throw new MyEntityNotFoundException("User not found: " + username);
        }
        return user;
    }

    public List<User> getAll() {
        return em.createNamedQuery("getAllUsers", User.class).getResultList();
    }

    public void update(String username, String email, String name) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.setEmail(email);
        user.setName(name);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_UPDATED,
                "User updated: " + username,
                "User",
                null,
                user
        );
    }

    public void changePassword(String username, String newPassword) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.setPassword(newPassword);

        // Log activity
        historyBean.logActivity(
                ActivityType.USER_UPDATED,
                "Password changed for user: " + username,
                "User",
                null,
                user
        );
    }

    public void changeRole(String username, Role newRole, User performedBy) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        Role oldRole = user.getRole();
        user.setRole(newRole);

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
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.setActive(true);

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
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.setActive(false);

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
            throw new IllegalArgumentException("User not found: " + username);
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

    public void subscribeTag(String username, Tag tag) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.subscribeTag(tag);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_SUBSCRIBED,
                "User " + username + " subscribed to tag: " + tag.getName(),
                "Tag",
                tag.getId(),
                user
        );
    }

    public void unsubscribeTag(String username, Tag tag) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        user.unsubscribeTag(tag);

        // Log activity
        historyBean.logActivity(
                ActivityType.TAG_UNSUBSCRIBED,
                "User " + username + " unsubscribed from tag: " + tag.getName(),
                "Tag",
                tag.getId(),
                user
        );
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
        if (user == null || !user.isActive()) {
            return false;
        }

        return Hasher.verify(password, user.getPassword());
    }
}
