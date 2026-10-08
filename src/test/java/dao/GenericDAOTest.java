package dao;

import com.fasterxml.jackson.annotation.JacksonInject;
import config.HibernateTestConfig;
import entities.Host;
import security.dao.UserDAO;
import security.entities.User;
import exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import io.javalin.http.HttpStatus;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericDAOTest {
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    JacksonInject.Value created;

    UserDAO userDAO;
    HostDAO hostDAO;

    @BeforeEach
    void beforeEach() {
        userDAO = new UserDAO(emf);
        hostDAO = new HostDAO(emf);


    }

    @BeforeEach
    void setUp() {
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
    void create() {
        //Testing user 1
        User user1 = new User("Lucas", "Lucas@mail.com", "Ek12");

        userDAO.create(user1);
        user1.getUserId();
        assertThat(user1.getUserId(), notNullValue());
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));

        //Testing user2
        User user2 = new User("Thomas", "Thomas@mail.com", "Ek13");

        userDAO.create(user2);
        user2.getUserId();
        assertThat(user2.getUserId(), notNullValue());
        User fetchedUser2 = userDAO.read(user2.getUserId());
        assertThat(fetchedUser2.getUserId(), is(user2.getUserId()));

        //Testing that user 1 is not user 2
        assertThat(fetchedUser1, not(user2));

        /////// HOST TEST

        //Testing new host
        Host host1 = new Host("Maskine1", "Firewall");

        hostDAO.create(host1);
        host1.getHostId();
        assertThat(host1.getHostId(), notNullValue());
        Host fetchedHost1 = hostDAO.read(host1.getHostId());
        assertThat(fetchedHost1.getHostId(), is(host1.getHostId()));

        //Testing a new host
        Host host2 = new Host("Maskine2", "osint");

        hostDAO.create(host2);
        host2.getHostId();
        assertThat(host2.getHostId(), notNullValue());
        Host fetchedHost2 = hostDAO.read(host2.getHostId());
        assertThat(fetchedHost2.getHostId(), is(host2.getHostId()));

        //Testing that they are not the same and can fail
        assertThat(fetchedHost2, not(host1));

        /////// GENERIC TEST

        assertThat(fetchedHost1, not(fetchedUser1));

    }

    @Test
    void update() {
        //Make user 1
        User user1 = new User("Lucas", "Lucas@mail.com", "Ek12");

        //Test user 1
        userDAO.create(user1);
        user1.getUserId();
        assertThat(user1.getUserId(), notNullValue());
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));

        //Update user 1 (password has no setter, so only email and username are updated)
        //Update user 1
        fetchedUser1.setEmail("LucasUpdated@mail");
        fetchedUser1.setUsername("LucasUpdated");
        fetchedUser1.changePassword("ek12Updated");
        userDAO.update(fetchedUser1);
        assertThat(fetchedUser1.getUserId(), notNullValue());

        //Get updated user 1
        User fetchedUser1Updated = userDAO.read(fetchedUser1.getUserId());
        assertThat(fetchedUser1Updated.getUserId(), is(fetchedUser1.getUserId()));

        //Compare user 1 updated to non updated
        //Not the same
        assertThat(fetchedUser1, not(fetchedUser1Updated));
        //Same ID
        assertThat(fetchedUser1.getUserId(), is(fetchedUser1Updated.getUserId()));
    }

    @Test
    void update_withNullUser_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.update(null));
        assertThat(ex.getCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void read() {
        //make user 1
        User user1 = new User("Lucas", "Lucas@mail.com", "Ek12");
        userDAO.create(user1);
        user1.getUserId();
        assertThat(user1.getUserId(), notNullValue());

        //Find user 1
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));

        //Make user2
        User user2 = new User("Thomas", "Thomas@mail.com", "Ek13");

        userDAO.create(user2);
        user2.getUserId();
        assertThat(user2.getUserId(), notNullValue());

        //Find user 1
        User fetchedUser2 = userDAO.read(user2.getUserId());
        assertThat(fetchedUser2.getUserId(), is(user2.getUserId()));


        //Compare
        assertThat(fetchedUser1, not(user2));
        assertThat(user2, not(user1));

    }

    @Test
    void delete() {
        //make user 1
        User user1 = new User("Lucas", "Lucas@mail.com", "Ek12");
        userDAO.create(user1);
        user1.getUserId();
        assertThat(user1.getUserId(), notNullValue());

        //Find user 1
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));

        //Delete user 1
        Boolean isDeleted = userDAO.delete(fetchedUser1.getUserId());

        //find user 1
        ApiException exception = assertThrows(ApiException.class, () -> {
            userDAO.read(user1.getUserId());
        });

        //Compare
        assertThat(exception.getCode(), is(HttpStatus.NOT_FOUND));
        assertThat(isDeleted, is(true));

    }

    @Test
    void readAll() {
        //Create 3 users
        User user1 = new User("Lucas", "Lucas@mail.com", "Ek12");
        User user2 = new User("ThomasH", "Thomas@mail.com", "Ek13");
        User user3 = new User("Jon", "Jon@mail.com", "Ek13");

        userDAO.create(user1);
        userDAO.create(user2);
        userDAO.create(user3);

        //Validate that they exist
        //Find user 1
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));
        //Find user 2
        User fetchedUser2 = userDAO.read(user2.getUserId());
        assertThat(fetchedUser2.getUserId(), is(user2.getUserId()));
        //Find user 3
        User fetchedUser3 = userDAO.read(user3.getUserId());
        assertThat(fetchedUser3.getUserId(), is(user3.getUserId()));

        //Get users
        List<User> userList = userDAO.readAll();
        //Test the actual size
        assertThat(userList, hasSize(3));
        //Test that it can fail
        assertThat(userList, not(hasSize(4)));
        assertThat(userList, not(hasSize(2)));

    }
}