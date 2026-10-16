package restassured;

import config.HibernateTestConfig;
import controller.HostController;
import controller.SoftwareController;
import dao.FindingDAO;
import dao.HostDAO;
import dao.SoftwareDAO;
import dao.VulnerabilityDAO;
import entities.Software;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import routes.HostRoutes;
import routes.SoftwareRoutes;
import security.controller.AccessController;
import security.controller.SecurityController;
import security.dao.UserDAO;
import security.routes.SecurityRoutes;
import utils.APIUtils;
import utils.Service;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoftwareRoutesTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private Javalin app;
    private String token;

    @BeforeAll
    void startServer() {
        //forbindelse til test server
        SecurityController.getInstance(emf);

        //DAO
        HostDAO hostDAO = new HostDAO(emf);
        SoftwareDAO softwareDAO = new SoftwareDAO(emf, Software.class);
        VulnerabilityDAO vulnerabilityDAO = new VulnerabilityDAO(emf);
        FindingDAO findingDAO = new FindingDAO(emf);
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
        HostRoutes hostRoutes = new HostRoutes(hostController);
        SoftwareRoutes softwareRoutes = new SoftwareRoutes(softwareController);
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

    @Test
    void createSoftware() {
        int hostid = createAHost("Firewall", "Placed U18");

        String name = "opnsense";
        String vendor = "opnsense";
        String version = "23.1";

        String body = "{\"softwareName\":\"" + name + "\",\"vendor\":\"" + vendor
                + "\",\"version\":\"" + version + "\"}";

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts/{hostId}/software", hostid)
                .then()
                .statusCode(201)
                .body("softwareName", equalTo("opnsense"))
                .body("vendor", equalTo("opnsense"))
                .body("version", equalTo("23.1"));
    }

    @Test
    void createSoftwareWithoutVendor() {
        int hostid = createAHost("Media", "U12");

        String name = "opnsense";
        String version = "23.1";

        String body = "{\"softwareName\":\"" + name
                + "\",\"version\":\"" + version + "\"}";


        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts/{hostId}/software", hostid)
                .then()
                .statusCode(400);
    }

    @Test
    void getSoftwareByHost() {
        int hostid = createAHost("Media", "U8");

        int softwareid1 = createASoftware(hostid, "jellyfin", "jellyfin", "10.8.0");
        int softwareid2 = createASoftware(hostid, "vlc_media_player", "videolan", "3.0.16");
        int softwareid3 = createASoftware(hostid, "kodi", "kodi", "18.0");

        List<String> names = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/{hostId}/software", hostid)
                .then()
                .statusCode(200)
                .extract()
                .path("softwareName");

        assertEquals(3, names.size());
        assertTrue(names.contains("jellyfin"));
        assertTrue(names.contains("vlc_media_player"));
        assertTrue(names.contains("kodi"));
    }

    @Test
    void getSoftwareById() {
        int hostid = createAHost("firewall", "U8");
        int softwareid1 = createASoftware(hostid, "opnsense", "opnsense", "23.1");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/software/{id}", softwareid1)
                .then()
                .statusCode(200)
                .body("softwareName", equalTo("opnsense"))
                .body("vendor", equalTo("opnsense"))
                .body("version", equalTo("23.1"));

    }

    @Test
    void getSoftwareThatDoesNotExist() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/software/{id}", 999)
                .then()
                .statusCode(404);

    }


    @Test
    void updateSoftware() {
        int hostid = createAHost("firewall", "U8");
        int softwareid1 = createASoftware(hostid, "opnsense", "opnsense", "23.1");

        //update
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("version", "24.0"))
                .when()
                .put("/software/{id}", softwareid1)
                .then()
                .statusCode(200)
                .body("version", equalTo("24.0"));

        //fetch for tjek
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/software/{id}", softwareid1)
                .then()
                .statusCode(200)
                .body("version", equalTo("24.0"));


    }

    @Test
    void deleteSoftware() {
        int hostid = createAHost("firewall", "U8");
        int softwareid1 = createASoftware(hostid, "opnsense", "opnsense", "23.1");

        // Slet hosten
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/software/{id}", softwareid1)
                .then()
                .statusCode(200);

        //tjek
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/software/{id}", softwareid1)
                .then()
                .statusCode(404);
    }


    @Test
    void getSoftwareWithoutToken() {
        int hostid = createAHost("firewall", "U8");
        int softwareid1 = createASoftware(hostid, "opnsense", "opnsense", "23.1");

        given()
                .when()
                .get("/software/{id}", softwareid1)
                .then()
                .statusCode(401);
    }
}