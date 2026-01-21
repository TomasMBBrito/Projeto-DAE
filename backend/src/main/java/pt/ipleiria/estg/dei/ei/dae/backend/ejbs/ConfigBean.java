package pt.ipleiria.estg.dei.ei.dae.backend.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.backend.entities.*;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyConstraintViolationException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.backend.exceptions.MyEntityNotFoundException;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@Singleton
@Startup
public class ConfigBean {

    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @EJB
    private UserBean userBean;

    @EJB
    private TagBean tagBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private RatingBean ratingBean;

    private Random random = new Random(42);


    @PostConstruct
    public void populateDB() {
        logger.info("========================================");
        logger.info("Initializing database with test data...");
        logger.info("========================================");

        try {
            // ==================== USERS ====================
            logger.info("Creating users...");

            List<User> allUsers = new ArrayList<>();
            List<User> admins = new ArrayList<>();
            List<User> responsaveis = new ArrayList<>();
            List<User> colaboradores = new ArrayList<>();

            // 3 ADMINISTRADORES
            User admin1 = userBean.create("admin1", "admin123", "admin1@research.pt", "Administrator One", Role.ADMINISTRADOR, null);
            User admin2 = userBean.create("admin2", "admin123", "admin2@research.pt", "Administrator Two", Role.ADMINISTRADOR, null);
            User admin3 = userBean.create("admin3", "admin123", "admin3@research.pt", "Administrator Three", Role.ADMINISTRADOR, null);

            admins.add(admin1);
            admins.add(admin2);
            admins.add(admin3);
            allUsers.addAll(admins);

            // 7 RESPONSÁVEIS
            User resp1 = userBean.create("resp1", "password123", "resp1@research.pt", "Responsável One", Role.RESPONSAVEL, null);
            User resp2 = userBean.create("resp2", "password123", "resp2@research.pt", "Responsável Two", Role.RESPONSAVEL, null);
            User resp3 = userBean.create("resp3", "password123", "resp3@research.pt", "Responsável Three", Role.RESPONSAVEL, null);
            User resp4 = userBean.create("resp4", "password123", "resp4@research.pt", "Responsável Four", Role.RESPONSAVEL, null);
            User resp5 = userBean.create("resp5", "password123", "resp5@research.pt", "Responsável Five", Role.RESPONSAVEL, null);
            User resp6 = userBean.create("resp6", "password123", "resp6@research.pt", "Responsável Six", Role.RESPONSAVEL, null);
            User resp7 = userBean.create("resp7", "password123", "resp7@research.pt", "Responsável Seven", Role.RESPONSAVEL, null);

            responsaveis.addAll(Arrays.asList(resp1, resp2, resp3, resp4, resp5, resp6, resp7));
            allUsers.addAll(responsaveis);

            // 10 COLABORADORES
            User colab1 = userBean.create("colab1", "password123", "colab1@research.pt", "Colaborador One", Role.COLABORADOR, null);
            User colab2 = userBean.create("colab2", "password123", "colab2@research.pt", "Colaborador Two", Role.COLABORADOR, null);
            User colab3 = userBean.create("colab3", "password123", "colab3@research.pt", "Colaborador Three", Role.COLABORADOR, null);
            User colab4 = userBean.create("colab4", "password123", "colab4@research.pt", "Colaborador Four", Role.COLABORADOR, null);
            User colab5 = userBean.create("colab5", "password123", "colab5@research.pt", "Colaborador Five", Role.COLABORADOR, null);
            User colab6 = userBean.create("colab6", "password123", "colab6@research.pt", "Colaborador Six", Role.COLABORADOR, null);
            User colab7 = userBean.create("colab7", "password123", "colab7@research.pt", "Colaborador Seven", Role.COLABORADOR, null);
            User colab8 = userBean.create("colab8", "password123", "colab8@research.pt", "Colaborador Eight", Role.COLABORADOR, null);
            User colab9 = userBean.create("colab9", "password123", "colab9@research.pt", "Colaborador Nine", Role.COLABORADOR, null);
            User colab10 = userBean.create("colab10", "password123", "colab10@research.pt", "Colaborador Ten", Role.COLABORADOR, null);

            colaboradores.addAll(Arrays.asList(colab1, colab2, colab3, colab4, colab5, colab6, colab7, colab8, colab9, colab10));
            allUsers.addAll(colaboradores);

            logger.info("✓ Created 20 users");

            // ==================== TAGS ====================
            logger.info("Creating tags...");

            List<Tag> allTags = new ArrayList<>();

            Tag tag1 = tagBean.create("Projeto X", admin1);
            Tag tag2 = tagBean.create("Projeto Y", admin1);
            Tag tag3 = tagBean.create("Projeto Z", admin2);
            Tag tag4 = tagBean.create("Inteligência Artificial", admin2);
            Tag tag5 = tagBean.create("Machine Learning", admin2);
            Tag tag6 = tagBean.create("Deep Learning", admin3);
            Tag tag7 = tagBean.create("Ciência de Dados", admin3);
            Tag tag8 = tagBean.create("Big Data", admin1);
            Tag tag9 = tagBean.create("IoT", admin1);
            Tag tag10 = tagBean.create("Cloud Computing", admin2);
            Tag tag11 = tagBean.create("Blockchain", admin2);
            Tag tag12 = tagBean.create("Cibersegurança", admin3);
            Tag tag13 = tagBean.create("Redes Neuronais", admin3);
            Tag tag14 = tagBean.create("Processamento de Linguagem Natural", admin1);
            Tag tag15 = tagBean.create("Visão Computacional", admin1);

            allTags.addAll(Arrays.asList(tag1, tag2, tag3, tag4, tag5, tag6, tag7, tag8, tag9, tag10, tag11, tag12, tag13, tag14, tag15));

            logger.info("✓ Created 15 tags");

            // ==================== SUBSCRIPTIONS ====================
            logger.info("Creating tag subscriptions...");

            int subscriptionCount = 0;
            for (User user : allUsers) {
                // Selecionar 3 tags aleatórias diferentes
                List<Tag> selectedTags = getRandomTags(allTags, 3);
                for (Tag tag : selectedTags) {
                    userBean.subscribeTag(user.getUsername(), tag.getId());
                    subscriptionCount++;
                }
            }

            logger.info("✓ Created tag subscriptions");

            // ==================== PUBLICATIONS ====================
            logger.info("Creating publications...");

            List<Publication> allPublications = new ArrayList<>();
            ScientificArea[] areas = ScientificArea.values();

            String[] titles = {
                    "Análise Avançada de Algoritmos de IA",
                    "Fundamentos de Machine Learning Aplicado",
                    "Redes Neuronais Profundas na Medicina",
                    "Processamento de Big Data em Tempo Real",
                    "Blockchain e Aplicações em Saúde",
                    "Segurança Cibernética em IoT",
                    "Cloud Computing para Análise de Dados",
                    "Visão Computacional em Robótica",
                    "Processamento de Linguagem Natural",
                    "Deep Learning para Diagnóstico Médico",
                    "Otimização de Algoritmos Genéticos",
                    "Análise Preditiva com Machine Learning",
                    "Sistemas Distribuídos e Escalabilidade",
                    "Computação Quântica e Aplicações",
                    "Redes de Sensores Inteligentes",
                    "Inteligência Artificial em Finanças",
                    "Análise de Sentimentos em Redes Sociais",
                    "Realidade Aumentada e Virtual",
                    "Ética em Inteligência Artificial",
                    "Aprendizagem por Reforço Profundo"
            };

            String[] descriptions = {
                    "Exploração detalhada de métodos modernos de IA.",
                    "Aplicação prática de técnicas de ML em projetos reais.",
                    "Uso de redes neuronais para diagnóstico médico automatizado.",
                    "Técnicas avançadas para processar grandes volumes de dados.",
                    "Implementação de blockchain em sistemas de saúde.",
                    "Estratégias de segurança para dispositivos IoT conectados.",
                    "Arquiteturas cloud para processamento distribuído.",
                    "Aplicações de visão computacional em robótica autónoma.",
                    "Técnicas modernas de NLP e suas aplicações.",
                    "Deep learning aplicado ao diagnóstico de doenças.",
                    "Otimização usando algoritmos evolutivos.",
                    "Modelos preditivos para análise de tendências.",
                    "Design de sistemas distribuídos de alta performance.",
                    "Introdução à computação quântica e potenciais aplicações.",
                    "Redes de sensores para monitorização ambiental.",
                    "IA aplicada a trading e análise financeira.",
                    "Análise automatizada de opiniões em redes sociais.",
                    "Tecnologias emergentes em AR/VR.",
                    "Considerações éticas no desenvolvimento de IA.",
                    "Aprendizagem por reforço em ambientes complexos."
            };

            for (int i = 0; i < 20; i++) {
                // Selecionar autor aleatório
                User author = allUsers.get(random.nextInt(allUsers.size()));

                // Selecionar área científica (ciclando pelo enum)
                ScientificArea area = areas[i % areas.length];

                // Selecionar 2-4 tags aleatórias
                int numTags = 2 + random.nextInt(3); // 2, 3 ou 4 tags
                List<Tag> selectedTags = getRandomTags(allTags, numTags);
                List<Long> tagIds = selectedTags.stream().map(Tag::getId).toList();

                // Data aleatória nos últimos 6 meses
                LocalDate date = LocalDate.now().minusDays(random.nextInt(180));

                // Tipo de ficheiro aleatório
                FileType fileType = random.nextBoolean() ? FileType.PDF : FileType.ZIP;
                String extension = fileType == FileType.PDF ? ".pdf" : ".zip";

                Publication pub = publicationBean.createWithoutFile(
                        titles[i],
                        descriptions[i],
                        area,
                        date,
                        Arrays.asList(author.getName(), "Co-autor " + (i + 1)),
                        author,
                        "publication_" + (i + 1) + extension,
                        fileType,
                        tagIds
                );

                allPublications.add(pub);
            }

            logger.info("✓ Created 20 publications");

            // ==================== COMMENTS ====================
            logger.info("Creating comments and ratings...");

            String[] commentTexts = {
                    "Excelente trabalho! Muito bem fundamentado.",
                    "Poderia incluir mais referências bibliográficas.",
                    "Abordagem muito interessante ao problema.",
                    "Alguns conceitos poderiam ser melhor explicados.",
                    "Artigo fundamental para a área.",
                    "Resultados impressionantes!",
                    "Metodologia bem estruturada.",
                    "Faltam alguns detalhes na implementação.",
                    "Contribuição significativa para o campo.",
                    "Discussão muito clara e objetiva.",
                    "Seria interessante explorar mais este tópico.",
                    "Concordo plenamente com as conclusões.",
                    "Trabalho inovador e bem executado.",
                    "Algumas limitações não foram abordadas.",
                    "Recomendo fortemente a leitura!",
                    "Análise muito completa.",
                    "Poderia incluir mais exemplos práticos.",
                    "Resultados alinhados com a literatura.",
                    "Excelente revisão do estado da arte.",
                    "Algumas questões ficaram em aberto."
            };

            int totalComments = 0;
            int totalRatings = 0;

            for (Publication pub : allPublications) {
                // Cada publicação recebe entre 1 e 5 comentários
                int numComments = 1 + random.nextInt(5); // 1 a 5

                // Selecionar users aleatórios para comentar (sem repetição na mesma pub)
                List<User> commenters = getRandomUsers(allUsers, numComments);

                for (int i = 0; i < numComments; i++) {
                    User commenter = commenters.get(i);
                    String commentText = commentTexts[random.nextInt(commentTexts.length)];

                    // Criar comentário
                    commentBean.create(commentText, commenter, pub);
                    totalComments++;

                    // O mesmo user que comentou dá um rating (1-5)
                    int ratingValue = 1 + random.nextInt(5); // 1 a 5
                    ratingBean.createOrUpdate(ratingValue, commenter, pub.getId());
                    totalRatings++;
                }
            }

            logger.info("✓ Created " + totalComments + " comments");
            logger.info("✓ Created " + totalRatings + " ratings");

            logger.info("Hiding some content for testing...");

            // Ocultar 2 publicações aleatórias
            List<Publication> pubsToHide = getRandomPublications(allPublications, 2);
            for (Publication pub : pubsToHide) {
                publicationBean.hide(pub.getId(), pub.getAuthor());
            }

            List<Tag> tagsToHide = getRandomTags(allTags, 2);
            for (Tag tag : tagsToHide) {
                tagBean.hide(tag.getId(), admin1.getUsername());
            }

            List<Comment> allComments = new ArrayList<>();
            for (Publication pub : allPublications) {
                allComments.addAll(pub.getComments());
            }

            if (allComments.size() >= 2) {
                List<Comment> commentsToHide = getRandomComments(allComments, 2);
                for (Comment comment : commentsToHide) {
                    commentBean.hide(comment.getId(), admin1);  // Admin pode ocultar comentários
                }
                logger.info("✓ Hidden 2 comments");
            } else {
                logger.warning("Not enough comments to hide");
            }

        } catch (MyEntityExistsException | MyConstraintViolationException | MyEntityNotFoundException e) {
            logger.severe("Error populating database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Tag> getRandomTags(List<Tag> allTags, int count) {
        List<Tag> shuffled = new ArrayList<>(allTags);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private List<User> getRandomUsers(List<User> allUsers, int count) {
        List<User> shuffled = new ArrayList<>(allUsers);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private List<Publication> getRandomPublications(List<Publication> allPubs, int count) {
        List<Publication> shuffled = new ArrayList<>(allPubs);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private List<Comment> getRandomComments(List<Comment> allComments, int count) {
        List<Comment> shuffled = new ArrayList<>(allComments);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }
}