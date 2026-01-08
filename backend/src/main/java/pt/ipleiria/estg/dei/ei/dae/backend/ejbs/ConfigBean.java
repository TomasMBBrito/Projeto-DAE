package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.backend.security.Hasher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
        logger.info("========================================");
        logger.info("Populating database with initial data...");
        logger.info("========================================");

        try {
            // Create users
            createUsers();

            // Create tags
            createTags();

            // Create publications
            createPublications();

            // Create comments
            createComments();

            // Create ratings
            createRatings();

            // Subscribe users to tags
            subscribeUsersToTags();

            logger.info("========================================");
            logger.info("Database population completed successfully!");
            logger.info("========================================");
        } catch (Exception e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createUsers() {

        try {
            // Administrators
            if (!userBean.exists("admin")) {
                userBean.create("admin", Hasher.hash("admin123"), "admin@ipleiria.pt",
                        "Administrador Principal", Role.ADMINISTRADOR);

            }

            // Responsáveis
            if (!userBean.exists("responsavel1")) {
                userBean.create("responsavel1", Hasher.hash("resp123"), "resp1@ipleiria.pt",
                        "Maria Santos", Role.RESPONSAVEL);

            }

            if (!userBean.exists("responsavel2")) {
                userBean.create("responsavel2", Hasher.hash("resp123"), "resp2@ipleiria.pt",
                        "Carlos Silva", Role.RESPONSAVEL);
            }

            if (!userBean.exists("joao.a")) {
                userBean.create("joao.a", Hasher.hash("joao123"), "joao.a@ipleiria.pt",
                        "João A", Role.COLABORADOR);

            }

            if (!userBean.exists("joana.b")) {
                userBean.create("joana.b", Hasher.hash("joana123"), "joana.b@ipleiria.pt",
                        "Joana B", Role.COLABORADOR);

            }

            if (!userBean.exists("manuel.c")) {
                userBean.create("manuel.c", Hasher.hash("manuel123"), "manuel.c@ipleiria.pt",
                        "Manuel C", Role.COLABORADOR);

            }

            if (!userBean.exists("ana.dias")) {
                userBean.create("ana.dias", Hasher.hash("ana123"), "ana.dias@ipleiria.pt",
                        "Ana Dias", Role.COLABORADOR);

            }

            if (!userBean.exists("pedro.lopes")) {
                userBean.create("pedro.lopes", Hasher.hash("pedro123"), "pedro.lopes@ipleiria.pt",
                        "Pedro Lopes", Role.COLABORADOR);
            }
        } catch (Exception e) {
            logger.severe("Error creating users: " + e.getMessage());
        }
    }

    private void createTags() {
        logger.info("Creating tags...");

        try {
            String[] tags = {
                    "Projeto X",      // From the project examples
                    "Projeto Y",      // From the project examples
                    "AI Research",
                    "Machine Learning",
                    "Deep Learning",
                    "Data Science",
                    "Healthcare",
                    "Climate Change",
                    "Quantum Computing",
                    "Blockchain",
                    "Cybersecurity",
                    "IoT",
                    "5G Networks",
                    "Renewable Energy"
            };

            User admin = userBean.find("admin");

            for (String tagName : tags) {
                if (!tagBean.exists(tagName)) {
                    tagBean.create(tagName, admin);
                    logger.info("✓ Created tag: " + tagName);
                }
            }
        } catch (Exception e) {
            logger.severe("Error creating tags: " + e.getMessage());
        }
    }

    private void createPublications() {
        logger.info("Creating publications...");

        try {
            User joaoA = userBean.find("joao.a");
            User joanaB = userBean.find("joana.b");
            User manuelC = userBean.find("manuel.c");
            User anaDias = userBean.find("ana.dias");
            User pedroLopes = userBean.find("pedro.lopes");

            // Publication 1 - Computer Science
            Publication pub1 = publicationBean.create(
                    "Deep Learning in Medical Imaging",
                    "A comprehensive study on the application of deep learning techniques in medical image analysis, focusing on tumor detection and classification.",
                    ScientificArea.COMPUTER_SCIENCE,
                    LocalDate.of(2024, 1, 15),
                    Arrays.asList("João A", "Maria Santos", "Carlos Oliveira"),
                    joaoA,
                    "deep-learning-medical-imaging.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub1.getId(), tagBean.findByName("AI Research"), joaoA);
            publicationBean.addTag(pub1.getId(), tagBean.findByName("Healthcare"), joaoA);
            publicationBean.addTag(pub1.getId(), tagBean.findByName("Deep Learning"), joaoA);
            logger.info("✓ Created publication: " + pub1.getTitle());

            // Publication 2 - Biology
            Publication pub2 = publicationBean.create(
                    "Climate Change Impact on Marine Ecosystems",
                    "Analysis of how rising ocean temperatures affect marine biodiversity and ecosystem dynamics.",
                    ScientificArea.BIOLOGY,
                    LocalDate.of(2024, 2, 10),
                    Arrays.asList("Ana Dias", "Paulo Costa"),
                    anaDias,
                    "climate-change-marine.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub2.getId(), tagBean.findByName("Climate Change"), anaDias);
            logger.info("✓ Created publication: " + pub2.getTitle());

            // Publication 3 - Projeto X (from examples)
            Publication pub3 = publicationBean.create(
                    "Novel Approach to Data Clustering",
                    "This paper presents a revolutionary technique for unsupervised learning that significantly improves clustering accuracy on large datasets.",
                    ScientificArea.COMPUTER_SCIENCE,
                    LocalDate.of(2024, 3, 5),
                    Arrays.asList("Joana B", "Ricardo Fernandes"),
                    joanaB,
                    "data-clustering-novel-approach.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub3.getId(), tagBean.findByName("Projeto X"), joanaB);
            publicationBean.addTag(pub3.getId(), tagBean.findByName("Machine Learning"), joanaB);
            publicationBean.addTag(pub3.getId(), tagBean.findByName("Data Science"), joanaB);
            logger.info("✓ Created publication: " + pub3.getTitle());

            // Publication 4 - Projeto X and Projeto Y (from examples)
            Publication pub4 = publicationBean.create(
                    "Advanced Algorithm for Pattern Recognition",
                    "A reference algorithm for pattern recognition with applications in both computer vision and signal processing. Contains implementation details.",
                    ScientificArea.COMPUTER_SCIENCE,
                    LocalDate.of(2023, 11, 20),
                    Arrays.asList("Manuel C", "Sofia Almeida"),
                    manuelC,
                    "pattern-recognition-algorithm.zip",
                    FileType.ZIP
            );
            publicationBean.addTag(pub4.getId(), tagBean.findByName("Projeto X"), manuelC);
            publicationBean.addTag(pub4.getId(), tagBean.findByName("Projeto Y"), manuelC);
            logger.info("✓ Created publication: " + pub4.getTitle());

            // Publication 5 - Physics
            Publication pub5 = publicationBean.create(
                    "Quantum Entanglement in Computing",
                    "Exploring the potential of quantum entanglement for next-generation computing systems.",
                    ScientificArea.PHYSICS,
                    LocalDate.of(2024, 1, 25),
                    Arrays.asList("Pedro Lopes", "Isabel Torres"),
                    pedroLopes,
                    "quantum-computing.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub5.getId(), tagBean.findByName("Quantum Computing"), pedroLopes);
            logger.info("✓ Created publication: " + pub5.getTitle());

            // Publication 6 - Engineering
            Publication pub6 = publicationBean.create(
                    "Blockchain Applications in Supply Chain",
                    "Comprehensive analysis of blockchain technology implementation in modern supply chain management.",
                    ScientificArea.ENGINEERING,
                    LocalDate.of(2024, 2, 28),
                    Arrays.asList("João A", "Luís Martins"),
                    joaoA,
                    "blockchain-supply-chain.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub6.getId(), tagBean.findByName("Blockchain"), joaoA);
            logger.info("✓ Created publication: " + pub6.getTitle());

            // Publication 7 - Cybersecurity
            Publication pub7 = publicationBean.create(
                    "AI-Powered Threat Detection Systems",
                    "Machine learning approaches to detecting and preventing cybersecurity threats in real-time.",
                    ScientificArea.COMPUTER_SCIENCE,
                    LocalDate.of(2024, 3, 10),
                    Arrays.asList("Ana Dias", "Miguel Rodrigues"),
                    anaDias,
                    "ai-threat-detection.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub7.getId(), tagBean.findByName("Cybersecurity"), anaDias);
            publicationBean.addTag(pub7.getId(), tagBean.findByName("AI Research"), anaDias);
            logger.info("✓ Created publication: " + pub7.getTitle());

            // Publication 8 - Renewable Energy
            Publication pub8 = publicationBean.create(
                    "Solar Panel Efficiency Optimization",
                    "Novel techniques for improving solar panel efficiency through advanced materials and smart positioning.",
                    ScientificArea.ENGINEERING,
                    LocalDate.of(2024, 1, 5),
                    Arrays.asList("Pedro Lopes", "Carla Sousa"),
                    pedroLopes,
                    "solar-panel-optimization.pdf",
                    FileType.PDF
            );
            publicationBean.addTag(pub8.getId(), tagBean.findByName("Renewable Energy"), pedroLopes);
            logger.info("✓ Created publication: " + pub8.getTitle());

        } catch (Exception e) {
            logger.severe("Error creating publications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createComments() {
        logger.info("Creating comments...");

        try {
            User joaoA = userBean.find("joao.a");
            User joanaB = userBean.find("joana.b");
            User manuelC = userBean.find("manuel.c");
            User anaDias = userBean.find("ana.dias");

            // Get publications
            List<Publication> publications = publicationBean.getAll();

            if (publications.size() >= 3) {
                // Example 2 from project: Joana B's enthusiastic comment on Projeto X publication
                commentBean.create(
                        "Parem com tudo! Esta nova técnica pode revolucionar a nossa abordagem ao Projeto X!",
                        joanaB,
                        publications.get(2) // "Novel Approach to Data Clustering"
                );
                logger.info("✓ Created comment by Joana B (Example 2)");

                // Example 3 from project: Manuel C's warning about error
                commentBean.create(
                        "AVISO: Há um erro grave neste artigo que pode afetar a implementação. Estou a trabalhar numa correção.",
                        manuelC,
                        publications.get(3) // "Advanced Algorithm for Pattern Recognition"
                );
                logger.info("✓ Created comment by Manuel C (Example 3)");

                // Additional comments
                commentBean.create(
                        "Excelente trabalho! Os resultados apresentados são muito promissores.",
                        joaoA,
                        publications.get(0)
                );

                commentBean.create(
                        "Interessante abordagem. Seria útil ver mais resultados experimentais.",
                        anaDias,
                        publications.get(0)
                );

                commentBean.create(
                        "Concordo com a análise apresentada. Muito relevante para o contexto atual.",
                        joaoA,
                        publications.get(1)
                );

                logger.info("✓ Created additional comments");
            }
        } catch (Exception e) {
            logger.severe("Error creating comments: " + e.getMessage());
        }
    }

    private void createRatings() {
        logger.info("Creating ratings...");

        try {
            User joaoA = userBean.find("joao.a");
            User joanaB = userBean.find("joana.b");
            User manuelC = userBean.find("manuel.c");
            User anaDias = userBean.find("ana.dias");
            User pedroLopes = userBean.find("pedro.lopes");

            List<Publication> publications = publicationBean.getAll();

            // Rate publications (creating variety in ratings)
            if (!publications.isEmpty()) {
                // Publication 1 - High ratings
                ratingBean.createOrUpdate(5, joanaB, publications.get(0));
                ratingBean.createOrUpdate(4, manuelC, publications.get(0));
                ratingBean.createOrUpdate(5, anaDias, publications.get(0));

                // Publication 2 - Mixed ratings
                ratingBean.createOrUpdate(4, joaoA, publications.get(1));
                ratingBean.createOrUpdate(3, pedroLopes, publications.get(1));

                // Publication 3 - Very high ratings (the revolutionary technique)
                ratingBean.createOrUpdate(5, joaoA, publications.get(2));
                ratingBean.createOrUpdate(5, manuelC, publications.get(2));
                ratingBean.createOrUpdate(4, pedroLopes, publications.get(2));

                // Publication 4 - Lower ratings (has the error mentioned by Manuel)
                ratingBean.createOrUpdate(2, joaoA, publications.get(3));
                ratingBean.createOrUpdate(3, joanaB, publications.get(3));

                logger.info("✓ Created ratings for publications");
            }
        } catch (Exception e) {
            logger.severe("Error creating ratings: " + e.getMessage());
        }
    }

    private void subscribeUsersToTags() {
        logger.info("Subscribing users to tags...");

        try {
            Tag projetoX = tagBean.findByName("Projeto X");
            Tag projetoY = tagBean.findByName("Projeto Y");
            Tag aiResearch = tagBean.findByName("AI Research");

            // Example 1: João A subscribes to Projeto X
            if (projetoX != null) {
                userBean.subscribeTag("joao.a", projetoX);
                logger.info("✓ João A subscribed to Projeto X (Example 1)");
            }

            // Joana B subscribes to Projeto X (she's very active on this project)
            if (projetoX != null) {
                userBean.subscribeTag("joana.b", projetoX);
                logger.info("✓ Joana B subscribed to Projeto X");
            }

            // Manuel C subscribes to both projects
            if (projetoX != null) {
                userBean.subscribeTag("manuel.c", projetoX);
                logger.info("✓ Manuel C subscribed to Projeto X");
            }
            if (projetoY != null) {
                userBean.subscribeTag("manuel.c", projetoY);
                logger.info("✓ Manuel C subscribed to Projeto Y");
            }

            // João A also subscribes to AI Research
            if (aiResearch != null) {
                userBean.subscribeTag("joao.a", aiResearch);
                logger.info("✓ João A subscribed to AI Research");
            }
        } catch (Exception e) {
            logger.severe("Error subscribing users to tags: " + e.getMessage());
        }
    }
}