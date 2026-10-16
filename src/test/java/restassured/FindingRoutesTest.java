package restassured;

import config.HibernateTestConfig;
import controller.FindingController;
import controller.HostController;
import controller.SoftwareController;
import dao.FindingDAO;
import dao.HostDAO;
import dao.SoftwareDAO;
import dao.VulnerabilityDAO;
import entities.Finding;
import entities.Metrics;
import entities.Software;
import entities.Vulnerability;
import exceptions.ApiException;
import exceptions.EntityException;
import exceptions.ValidationException;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import mapper.FindingMapper;
import mapper.HostMapper;
import mapper.NVDMapper;
import mapper.SoftwareMapper;
import org.junit.jupiter.api.*;
import routes.FindingRoutes;
import routes.HostRoutes;
import routes.SoftwareRoutes;
import security.controller.AccessController;
import security.controller.SecurityController;
import security.dao.UserDAO;
import security.routes.SecurityRoutes;
import utils.APIUtils;
import utils.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FindingRoutesTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private Javalin app;
    private String token;

    private HostDAO hostDAO;
    private SoftwareDAO softwareDAO;
    private VulnerabilityDAO vulnerabilityDAO;
    private FindingDAO findingDAO;

    @BeforeAll
    void startServer() {
        //forbindelse til test server
        SecurityController.getInstance(emf);

        //DAO
        hostDAO = new HostDAO(emf);
        softwareDAO = new SoftwareDAO(emf, Software.class);
        vulnerabilityDAO = new VulnerabilityDAO(emf);
        findingDAO = new FindingDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        //Mappers
        HostMapper hostMapper = new HostMapper();
        SoftwareMapper softwareMapper = new SoftwareMapper();
        FindingMapper findingMapper = new FindingMapper();

        //Service
        Service service = new Service(new APIUtils(), new NVDMapper(),
                softwareDAO, vulnerabilityDAO, findingDAO, null);

        //Controllere og routes
        HostController hostController = new HostController(hostDAO, hostMapper, userDAO);
        SoftwareController softwareController =
                new SoftwareController(softwareMapper, softwareDAO, userDAO, hostDAO, service, findingMapper);
        FindingController findingController =
                new FindingController(findingMapper, findingDAO, userDAO);

        HostRoutes hostRoutes = new HostRoutes(hostController);
        SoftwareRoutes softwareRoutes = new SoftwareRoutes(softwareController);
        FindingRoutes findingRoutes = new FindingRoutes(findingController);
        SecurityRoutes securityRoutes = new SecurityRoutes();

        //Rest
        app = Javalin.create(config -> {
            config.router.contextPath = "/api";
            config.router.apiBuilder(() -> {
                path("/auth", securityRoutes.getRoutes());
                path("/hosts", () -> {
                    hostRoutes.getRoutes().addEndpoints();
                    path("/{hostId}/software", softwareRoutes.getHostScopedRoutes());
                });
                path("/software", softwareRoutes.getRoutes());
                path("/findings", findingRoutes.getRoutes()); // TJEK: metodenavn
            });
        });

        //token og roller
        AccessController accessController = new AccessController();
        app.beforeMatched(accessController::accessHandler);

        //Exceptions
        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("msg", e.getMessage()));
        });
        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("msg", e.getMessage()));
        });
        app.exception(EntityException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("msg", e.getMessage()));
        });

        //Start test port
        app.start(7777);

        //Path til restAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7777;
        RestAssured.basePath = "/api";
    }

    @BeforeEach
    void delDatabase() {
        EntityManager em = emf.createEntityManager();

        //Clean tables
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Finding").executeUpdate();
        em.createQuery("DELETE FROM Vulnerability").executeUpdate();
        em.createQuery("DELETE FROM Software").executeUpdate();
        em.createQuery("DELETE FROM Host").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.getTransaction().commit();
        em.close();

        token = given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas", "email", "a@b.dk", "password", "test"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .extract()
                .path("token");
    }

    @AfterAll
    void shutdown() {
        app.stop();
    }

    //Helping methods

    private int createAHost(String name, String description) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("hostname", name, "hostDescription", description))
                .when()
                .post("/hosts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private int createASoftware(int hostId, String name, String vendor, String version) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("softwareName", name, "vendor", vendor, "version", version))
                .when()
                .post("/hosts/{hostId}/software", hostId)
                .then()
                .statusCode(201)
                .extract()
                .path("softwareId");
    }

    private int createAFinding(int softwareId, String cveId, double cvssScore, String severity) {
        Software software = softwareDAO.read((long) softwareId);

        Metrics metrics = Metrics.builder()
                .cvssScore(cvssScore)
                .severity(severity)
                .build();

        Vulnerability vulnerability = Vulnerability.builder()
                .vulnerabilityId(cveId)
                .description("Test vulnerability " + cveId)
                .metrics(metrics)
                .build();

        vulnerabilityDAO.create(vulnerability);

        Finding finding = Finding.builder()
                .software(software)
                .vulnerability(vulnerability)
                .detectedAt(LocalDateTime.now())
                .build();

        Finding saved = findingDAO.create(finding);

        return saved.getFindingId().intValue();
    }

    //Test

    @Test
    void updateFindingStatus() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int findingid = createAFinding(softwareid, "CVE-2024-0001", 9.8, "CRITICAL");

        //update
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "IN_PROGRESS"))
                .when()
                .put("/findings/{id}/status", findingid)
                .then()
                .statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("resolvedAt", nullValue());

        //fetch for tjek
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/findings/status/asc")
                .then()
                .statusCode(200)
                .body("status", hasItem("IN_PROGRESS"));
    }

    @Test
    void updateFindingStatusToResolved() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int findingid = createAFinding(softwareid, "CVE-2024-0001", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "RESOLVED"))
                .when()
                .put("/findings/{id}/status", findingid)
                .then()
                .statusCode(200)
                .body("status", equalTo("RESOLVED"))
                .body("resolvedAt", notNullValue());
    }

    @Test
    void updateFindingStatusWithoutStatus() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int findingid = createAFinding(softwareid, "CVE-2024-0001", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .put("/findings/{id}/status", findingid)
                .then()
                .statusCode(400);
    }


    @Test
    void updateStatusOnFindingThatDoesNotExist() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "IN_PROGRESS"))
                .when()
                .put("/findings/{id}/status", 999)
                .then()
                .statusCode(404);
    }

    @Test
    void updateStatusWithoutToken() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int findingid = createAFinding(softwareid, "CVE-2024-0001", 9.8, "CRITICAL");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("status", "IN_PROGRESS"))
                .when()
                .put("/findings/{id}/status", findingid)
                .then()
                .statusCode(401);
    }

    //cvss
    @Test
    void getFindingsSortedByCvssDescending() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        createAFinding(softwareid, "CVE-2024-0001", 3.1, "LOW");
        createAFinding(softwareid, "CVE-2024-0002", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/findings/cvss/desc")
                .then()
                .statusCode(200)
                .body("cvssScore", contains(9.8f, 3.1f));
    }

    @Test
    void getFindingsSortedByCvssAscending() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        createAFinding(softwareid, "CVE-2024-0001", 3.1, "LOW");
        createAFinding(softwareid, "CVE-2024-0002", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/findings/cvss/asc")
                .then()
                .statusCode(200)
                .body("cvssScore", contains(3.1f, 9.8f));
    }

    //status
    @Test
    void getFindingsSortedByStatusAscending() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int finding1 = createAFinding(softwareid, "CVE-2024-0001", 3.1, "LOW");
        int finding2 = createAFinding(softwareid, "CVE-2024-0002", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "RESOLVED"))
                .when()
                .put("/findings/{id}/status", finding1)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/findings/status/asc")
                .then()
                .statusCode(200)
                .body("status", contains("OPEN", "RESOLVED"));
    }

    @Test
    void getFindingsSortedByStatusDescending() {
        int hostid = createAHost("firewall", "U8");
        int softwareid = createASoftware(hostid, "opnsense", "opnsense", "23.1");
        int finding1 = createAFinding(softwareid, "CVE-2024-0001", 3.1, "LOW");
        int finding2 = createAFinding(softwareid, "CVE-2024-0002", 9.8, "CRITICAL");

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("status", "RESOLVED"))
                .when()
                .put("/findings/{id}/status", finding1)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/findings/status/desc")
                .then()
                .statusCode(200)
                .body("status", contains("RESOLVED", "OPEN"));
    }
}
