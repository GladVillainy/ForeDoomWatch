package restassured;

import config.HibernateTestConfig;
import exceptions.ApiException;
import exceptions.ValidationException;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import security.controller.SecurityController;
import security.routes.SecurityRoutes;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Rest Assure test on security login and register
 * All test passed
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private Javalin app;

    @BeforeAll
    void startServer() {
        //forbindelse til test server
        SecurityController.getInstance(emf);

        //routing
        SecurityRoutes securityRoutes = new SecurityRoutes();
        app = Javalin.create(config -> {
            config.router.contextPath = "/api";
            config.router.apiBuilder(() -> {
                path("/auth", securityRoutes.getRoutes());
            });
        });
        //Exceptions til json
        app.exception(ApiException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("msg", e.getMessage()));
        });
        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(e.getCode());
            ctx.json(Map.of("msg", e.getMessage()));
        });

        app.start(7777);

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
    }


    @AfterAll
    void shutdown() {
        emf.close();
    }


    @Test
    void registration() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas", "email", "a@b.dk", "password", "test"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("username", equalTo("Lucas"),
                        "email", equalTo("a@b.dk"));
    }

    @Test
    void registrationWithIdenticalMail(){
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas1", "email", "a@b.dk", "password", "test1"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas2", "email", "a@b.dk", "password", "test2"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(409);
    }

    @Test
    void registrationWithInvaildEmail() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas", "email", "abc", "password", "test"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(400);
    }

    @Test
    void login() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas", "email", "a@b.dk", "password", "test"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of( "email", "a@b.dk", "password", "test"))
        .when()
                .post("/auth/login")
        .then()
                .body("token", notNullValue())
                .statusCode(200);

    }

    @Test
    void loginWithWrongPassword() {

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("username", "Lucas", "email", "a@b.dk", "password", "test"))
        .when()
                .post("/auth/register")
        .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of( "email", "a@b.dk", "password", "wrong"))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(401);
    }
}
