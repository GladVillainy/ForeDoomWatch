package restassured;

import config.HibernateTestConfig;
import controller.HostController;
import dao.HostDAO;
import exceptions.ApiException;
import exceptions.EntityException;
import exceptions.ValidationException;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import mapper.HostMapper;
import org.junit.jupiter.api.*;
import routes.HostRoutes;
import security.controller.AccessController;
import security.controller.SecurityController;
import security.dao.UserDAO;
import security.routes.SecurityRoutes;

import java.util.List;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HostRoutesTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private Javalin app;
    private String token;

    @BeforeAll
    void startServer() {
        //forbindelse til test server
        SecurityController.getInstance(emf);

        //lagene
        HostDAO hostDAO = new HostDAO(emf);
        UserDAO userDAO = new UserDAO(emf);
        HostMapper hostMapper = new HostMapper();
        HostController hostController = new HostController(hostDAO, hostMapper, userDAO);
        HostRoutes hostRoutes = new HostRoutes(hostController);

        //routing til login
        SecurityRoutes securityRoutes = new SecurityRoutes();

        //Rest
        app = Javalin.create(config -> {
            config.router.contextPath = "/api";
            config.router.apiBuilder(() -> {
                path("/auth", securityRoutes.getRoutes());
                path("/hosts", hostRoutes.getRoutes());
            });
        });

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

        //Path til restAsured
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

    @Test
    void createHost() {
        String name = "Firewall";
        String description = "Runs open source OPNsense and Proton VPN";

        String body = "{\"hostname\":\"" + name + "\",\"hostDescription\":\"" + description + "\"}";


        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts")
                .then()
                .statusCode(201)
                .body("hostname", equalTo("Firewall"))
                .body("hostDescription", equalTo("Runs open source OPNsense and Proton VPN"));

    }

    @Test
    void createHostNoName() {
        String description = "Runs open source OPNsense and Proton VPN";

        String body = "{\"hostname\":\"\",\"hostDescription\":\"" + description + "\"}";


        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts")
                .then()
                .statusCode(400);
    }

    @Test
    void getHostsById() {
        int id1 = createAHost("media", "hostes jellyfin");
        int id2 = createAHost("firewall", "OPNsense");
        int id3 = createAHost("nas", "RAID3");

        List<String> names = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/")
                .then()
                .statusCode(200)
                .extract()
                .path("hostname");

        assertEquals(3, names.size());
        assertTrue(names.contains("firewall"));
        assertTrue(names.contains("media"));
        assertTrue(names.contains("nas"));
    }


    @Test
    void getOneHost() {
        int id1 = createAHost("media", "hostes jellyfin");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/{id}", id1)
                .then()
                .statusCode(200)
                .body("hostname", equalTo("media"))
                .body("hostDescription", equalTo("hostes jellyfin"));
    }

    @Test
    void getHostDoestExist() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/{id}", 999)
                .then()
                .statusCode(404);
    }

    @Test
    void updateHost() {
        int id = createAHost("old name", "old description");

        //update
        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("hostname", "new name"))
                .when()
                .put("/hosts/{id}", id)
                .then()
                .statusCode(200)
                .body("hostname", equalTo("new name"))
                .body("hostDescription", equalTo("old description"));

        // get, og se ændringen
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/{id}", id)
                .then()
                .statusCode(200)
                .body("hostname", equalTo("new name"))
                .body("hostDescription", equalTo("old description"));
    }

    @Test
    void deleteHost() {
        int id = createAHost("delete", "deleting test");

        // Slet hosten
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/hosts/{id}", id)
                .then()
                .statusCode(200);

        // Tjek
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/hosts/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void createNoTokenHost() {
        String description = "Runs open source OPNsense and Proton VPN";

        String body = "{\"hostname\":\"\",\"hostDescription\":\"" + description + "\"}";
        String createdName = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts")
                .then()
                .statusCode(401)
                .extract()
                .path("hostname");

    }

    private int createAHost(String name, String description) {
        String body = "{\"hostname\":\"" + name + "\",\"hostDescription\":\"" + description + "\"}";

        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/hosts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }


}