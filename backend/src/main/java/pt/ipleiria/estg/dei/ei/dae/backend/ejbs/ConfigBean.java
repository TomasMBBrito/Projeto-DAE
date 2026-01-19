package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import java.util.logging.Logger;

@Singleton
@Startup
public class ConfigBean {

    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @EJB
    private UserBean userBean;


    @PostConstruct
    public void populateDB() {
        logger.info("Initializing database with default data...");

        try {
                userBean.create("admin", "admin123", "admin@example.com", "Administrador", Role.ADMINISTRADOR, null);
                userBean.create("user1", "password123", "user1@example.com", "Utilizador 1", Role.RESPONSAVEL,null);
                userBean.create("user2", "password123", "user2@example.com", "Utilizador 2", Role.COLABORADOR,null);

            logger.info("Database populated successfully!");

        } catch (Exception e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}