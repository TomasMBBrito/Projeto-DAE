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
import java.util.Arrays;
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


    @PostConstruct
    public void populateDB() {
        logger.info("========================================");
        logger.info("Initializing database with test data...");
        logger.info("========================================");

        try {
            // ==================== USERS ====================
            logger.info("Creating users...");

            User admin = userBean.create(
                    "admin",
                    "admin123",
                    "admin@research.pt",
                    "Administrator System",
                    Role.ADMINISTRADOR,
                    null
            );

            User joanaB = userBean.create(
                    "joana",
                    "password123",
                    "joana.b@research.pt",
                    "Joana B",
                    Role.RESPONSAVEL,
                    null
            );

            User joaoA = userBean.create(
                    "joao",
                    "password123",
                    "joao.a@research.pt",
                    "João A",
                    Role.COLABORADOR,
                    null
            );

            User manuelC = userBean.create(
                    "manuel",
                    "password123",
                    "manuel.c@research.pt",
                    "Manuel C",
                    Role.COLABORADOR,
                    null
            );

            User anaF = userBean.create(
                    "ana",
                    "password123",
                    "ana.f@research.pt",
                    "Ana Ferreira",
                    Role.COLABORADOR,
                    null
            );

            User brunoC = userBean.create(
                    "bruno",
                    "password123",
                    "bruno.c@research.pt",
                    "Bruno Costa",
                    Role.COLABORADOR,
                    null
            );

            logger.info("✓ Created 6 users (1 Admin, 1 Responsável, 4 Colaboradores)");

            // ==================== TAGS ====================
            logger.info("Creating tags...");

            Tag tagProjetoX = tagBean.create("Projeto X", admin);
            Tag tagProjetoY = tagBean.create("Projeto Y", admin);
            Tag tagIA = tagBean.create("IA", admin);
            Tag tagCienciaDados = tagBean.create("Ciência de Dados", admin);
            Tag tagMachineLearning = tagBean.create("Machine Learning", admin);
            Tag tagBaseDados = tagBean.create("Base de Dados", admin);
            Tag tagAlgoritmos = tagBean.create("Algoritmos", admin);

            logger.info("✓ Created 7 tags");

            // ==================== SUBSCRIPTIONS ====================
            logger.info("Creating tag subscriptions...");

            // João A subscreve Projeto X (Exemplo 1)
            userBean.subscribeTag("joao", tagProjetoX.getId());

            // Joana B subscreve Projeto X
            userBean.subscribeTag("joana", tagProjetoX.getId());

            // Manuel C subscreve Projeto X e Y (Exemplo 3)
            userBean.subscribeTag("manuel", tagProjetoX.getId());
            userBean.subscribeTag("manuel", tagProjetoY.getId());

            // Ana subscreve IA e Ciência de Dados
            userBean.subscribeTag("ana", tagIA.getId());
            userBean.subscribeTag("ana", tagCienciaDados.getId());

            logger.info("✓ Created tag subscriptions");

            // ==================== PUBLICATIONS ====================
            logger.info("Creating publications...");

            // Publicação 1: Análise de Algoritmos de IA (João A)
            Publication pub1 = publicationBean.createWithoutFile(
                    "Análise de Algoritmos de IA",
                    "Exploração detalhada de métodos de IA e algoritmos heurísticos aplicados a problemas complexos.",
                    ScientificArea.CHEMISTRY,
                    LocalDate.of(2024, 10, 18),
                    Arrays.asList("João Silva", "Maria Santos"),
                    joaoA,
                    "analise_algoritmos_ia.pdf",
                    FileType.PDF,
                    Arrays.asList(tagIA.getId(), tagCienciaDados.getId(), tagAlgoritmos.getId())
            );

            // Publicação 2: Ciência por trás de uma base de dados (Ana)
            Publication pub2 = publicationBean.createWithoutFile(
                    "Ciência por trás de uma base de dados",
                    "Discussão aprofundada sobre a estrutura, otimização e design de bases de dados relacionais.",
                    ScientificArea.ENGINEERING,
                    LocalDate.of(2024, 10, 20),
                    Arrays.asList("Ana Ferreira", "Pedro Costa"),
                    anaF,
                    "ciencia_base_dados.pdf",
                    FileType.PDF,
                    Arrays.asList(tagBaseDados.getId(), tagCienciaDados.getId())
            );

            // Publicação 3: Algoritmos Evolutivos em Machine Learning (Bruno)
            Publication pub3 = publicationBean.createWithoutFile(
                    "Algoritmos Evolutivos em Machine Learning",
                    "Discussão sobre otimização baseada em evolução aplicada ao machine learning.",
                    ScientificArea.HUMANITIES,
                    LocalDate.of(2024, 11, 5),
                    Arrays.asList("Bruno Costa"),
                    brunoC,
                    "algoritmos_evolutivos.pdf",
                    FileType.PDF,
                    Arrays.asList(tagMachineLearning.getId(), tagIA.getId(), tagAlgoritmos.getId())
            );

            // Publicação 4: Nova técnica revolucionária (Joana B - Exemplo 2)
            Publication pub4 = publicationBean.createWithoutFile(
                    "Nova Técnica de Otimização para Projeto X",
                    "Técnica inovadora que pode revolucionar a abordagem ao Projeto X.",
                    ScientificArea.MATHEMATICS,
                    LocalDate.now(),
                    Arrays.asList("Dr. Investigador Externo"),
                    joanaB,
                    "nova_tecnica_projeto_x.pdf",
                    FileType.PDF,
                    Arrays.asList(tagProjetoX.getId(), tagIA.getId())
            );

            // Publicação 5: Artigo de referência com erro (Exemplo 3)
            Publication pub5 = publicationBean.createWithoutFile(
                    "Implementação de Algoritmo de Referência",
                    "Artigo de referência para implementação de algoritmo usado nos Projetos X e Y.",
                    ScientificArea.BIOLOGY,
                    LocalDate.of(2024, 9, 15),
                    Arrays.asList("Autor Principal", "Co-autor"),
                    anaF,
                    "algoritmo_referencia.pdf",
                    FileType.PDF,
                    Arrays.asList(tagProjetoX.getId(), tagProjetoY.getId(), tagAlgoritmos.getId())
            );

            // Publicação 6: Redes Neuronais Convolucionais (recente)
            Publication pub6 = publicationBean.createWithoutFile(
                    "Redes Neuronais Convolucionais",
                    "Exploração detalhada de redes neuronais convolucionais e suas aplicações.",
                    ScientificArea.SOCIAL_SCIENCES,
                    LocalDate.of(2024, 12, 25),
                    Arrays.asList("Ana Ferreira", "João Silva"),
                    anaF,
                    "redes_neuronais.pdf",
                    FileType.PDF,
                    Arrays.asList(tagIA.getId(), tagMachineLearning.getId())
            );

            // Publicação 7: Aprendizagem por Reforço
            Publication pub7 = publicationBean.createWithoutFile(
                    "Aprendizagem por Reforço",
                    "Discussão sobre o método de aprendizagem por reforço e suas vantagens práticas.",
                    ScientificArea.ENGINEERING,
                    LocalDate.of(2024, 12, 20),
                    Arrays.asList("João Silva"),
                    joaoA,
                    "aprendizagem_reforco.pdf",
                    FileType.PDF,
                    Arrays.asList(tagIA.getId(), tagMachineLearning.getId())
            );

            // Publicação 8: Dataset de Ciência de Materiais (ZIP)
            Publication pub8 = publicationBean.createWithoutFile(
                    "Dataset Completo de Propriedades de Materiais",
                    "Conjunto de dados científicos sobre propriedades térmicas e mecânicas de materiais.",
                    ScientificArea.MATHEMATICS,
                    LocalDate.of(2024, 11, 10),
                    Arrays.asList("Manuel C", "Equipa de Investigação"),
                    manuelC,
                    "dataset_materiais.zip",
                    FileType.ZIP,
                    Arrays.asList(tagProjetoY.getId())
            );

            // Publicação 9: Publicação não visível (teste)
            Publication pub9 = publicationBean.createWithoutFile(
                    "Artigo Confidencial em Desenvolvimento",
                    "Trabalho em progresso sobre nova abordagem.",
                    ScientificArea.MEDICINE,
                    LocalDate.now(),
                    Arrays.asList("Bruno Costa"),
                    brunoC,
                    "confidencial.pdf",
                    FileType.PDF,
                    Arrays.asList(tagIA.getId())
            );
            publicationBean.hide(pub9.getId(), brunoC);

            logger.info("✓ Created 9 publications");

            // ==================== COMMENTS ====================
            logger.info("Creating comments...");

            // Comentários na pub1 (Análise de Algoritmos de IA)
            Comment comment1 = commentBean.create(
                    "Excelente artigo! Muito bem explicado.",
                    anaF,
                    pub1
            );

            Comment comment2 = commentBean.create(
                    "Poderia incluir mais referências sobre algoritmos genéticos.",
                    brunoC,
                    pub1
            );

            // Comentário de Joana B na pub4 (Exemplo 2 do enunciado)
            Comment comment3 = commentBean.create(
                    "Parem com tudo! Esta nova técnica pode revolucionar a nossa abordagem ao Projeto X!",
                    joanaB,
                    pub4
            );

            // Comentário de Manuel C no artigo com erro (Exemplo 3)
            Comment comment4 = commentBean.create(
                    "ATENÇÃO: Detectei um erro grave neste artigo na secção 3.2. O algoritmo apresentado tem uma falha que pode levar a resultados incorretos. Estou a preparar um relatório detalhado.",
                    manuelC,
                    pub5
            );

            Comment comment5 = commentBean.create(
                    "Obrigado pelo aviso, Manuel! Vou suspender a implementação até termos mais detalhes.",
                    joaoA,
                    pub5
            );

            // Mais comentários
            Comment comment6 = commentBean.create(
                    "Muito interessante a abordagem apresentada.",
                    joaoA,
                    pub3
            );

            Comment comment7 = commentBean.create(
                    "Este trabalho é fundamental para o Projeto X.",
                    joanaB,
                    pub1
            );

            // Comentário oculto (teste de visibilidade)
            Comment comment8 = commentBean.create(
                    "Comentário que será ocultado para teste.",
                    brunoC,
                    pub2
            );
            commentBean.hide(comment8.getId(), admin);

            logger.info("✓ Created 8 comments (1 hidden)");

            // ==================== RATINGS ====================
            logger.info("Creating ratings...");

            // Ratings para pub1 (média alta)
            ratingBean.createOrUpdate(5, anaF, pub1.getId());
            ratingBean.createOrUpdate(5, brunoC, pub1.getId());
            ratingBean.createOrUpdate(4, manuelC, pub1.getId());
            ratingBean.createOrUpdate(5, joanaB, pub1.getId());

            // Ratings para pub2
            ratingBean.createOrUpdate(4, joaoA, pub2.getId());
            ratingBean.createOrUpdate(5, brunoC, pub2.getId());
            ratingBean.createOrUpdate(4, manuelC, pub2.getId());

            // Ratings para pub3
            ratingBean.createOrUpdate(5, joaoA, pub3.getId());
            ratingBean.createOrUpdate(4, anaF, pub3.getId());

            // Ratings para pub4 (técnica revolucionária)
            ratingBean.createOrUpdate(5, joaoA, pub4.getId());
            ratingBean.createOrUpdate(5, manuelC, pub4.getId());

            // Ratings para pub5 (artigo com erro - ratings mais baixos)
            ratingBean.createOrUpdate(2, joaoA, pub5.getId());
            ratingBean.createOrUpdate(3, brunoC, pub5.getId());

            // Ratings para pub6 (mais recente)
            ratingBean.createOrUpdate(5, joaoA, pub6.getId());
            ratingBean.createOrUpdate(5, brunoC, pub6.getId());
            ratingBean.createOrUpdate(4, manuelC, pub6.getId());

            // Ratings para pub7
            ratingBean.createOrUpdate(4, anaF, pub7.getId());
            ratingBean.createOrUpdate(5, brunoC, pub7.getId());

            logger.info("✓ Created 20 ratings");

            // ==================== SUMMARY ====================
            logger.info("========================================");
            logger.info("Database population completed!");
            logger.info("========================================");
            logger.info("Summary:");
            logger.info("  • 6 Users (1 Admin, 1 Responsável, 4 Colaboradores)");
            logger.info("  • 7 Tags");
            logger.info("  • 9 Publications (1 hidden)");
            logger.info("  • 8 Comments (1 hidden)");
            logger.info("  • 20 Ratings");
            logger.info("  • Multiple tag subscriptions");
            logger.info("========================================");
            logger.info("Test accounts:");
            logger.info("  Admin:        admin / admin123");
            logger.info("  Responsável:  joana / password123");
            logger.info("  Colaborador:  joao / password123");
            logger.info("  Colaborador:  manuel / password123");
            logger.info("  Colaborador:  ana / password123");
            logger.info("  Colaborador:  bruno / password123");
            logger.info("========================================");

        } catch (MyEntityExistsException | MyConstraintViolationException | MyEntityNotFoundException e) {
            logger.severe("❌ Error populating database: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}