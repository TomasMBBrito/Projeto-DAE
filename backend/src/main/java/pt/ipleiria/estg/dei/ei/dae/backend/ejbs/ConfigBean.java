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

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private TagBean tagBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private RatingBean ratingBean;

    @PostConstruct
    public void populateDB() {
        logger.info("Initializing database with default data...");

        try {
            User user = userBean.create(
                    "admin",
                    "admin123",
                    "admin@xyz.pt",
                    "Admin Sistema",
                    Role.ADMINISTRADOR,
                    null
            );
            // Criar tags
            Tag tag = tagBean.create("Projeto X", user);
            tagBean.create("Projeto Y", user);
            tagBean.create("IA", user);
            tagBean.create("Ciência de Dados", user);
            tagBean.create("Ciência dos Materiais", user);
            tagBean.create("Machine Learning", user);

            // Responsáveis
            userBean.create("maria",
                    "maria123",
                    "maria@xyz.pt",
                    "Maria Silva",
                    Role.RESPONSAVEL,
                    user
            );

            userBean.create(
                    "pedro",
                    "123456",
                    "pedro@xyz",
                    "Pedro Santos",
                    Role.RESPONSAVEL,
                    user
            );

            // Colaboradores
            userBean.create(
                    "joao",
                    "123456",
                    "joao@xyz.pt",
                    "João Almeida",
                    Role.COLABORADOR,
                    user
            );

            userBean.create(
                    "joana",
                    "123456",
                    "joana@xyz.pt",
                    "Joana Pereira",
                    Role.COLABORADOR,
                    user
            );

            userBean.create(
                    "bruno",
                    "123456",
                    "bruno@mail.pt",
                    "Bruno Costa",
                    Role.COLABORADOR,
                    user
            );

            // Bloquear o Bruno como exemplo
            userBean.deactivate("bruno", user);
            userBean.subscribeTag("joao",tag.getId() );
            userBean.subscribeTag("joana",tag.getId() );

            logger.info("Database populated successfully!");

        } catch (Exception e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}