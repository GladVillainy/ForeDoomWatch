package dao;


import com.fasterxml.jackson.annotation.JacksonInject;
import config.HibernateTestConfig;
import entities.Host;
import entities.User;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


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

    @AfterAll
    void shutdown() {
        emf.close();
    }


    @Test
    void create() {
        //Testing user 1
        User user1 = new User("Lucas@mail.com", "Lucas", "Ek12");

        userDAO.create(user1);
        user1.getUserId();
        assertThat(user1.getUserId(), notNullValue());
        User fetchedUser1 = userDAO.read(user1.getUserId());
        assertThat(fetchedUser1.getUserId(), is(user1.getUserId()));

        //Testing user2
        User user2 = new User("Thomas@mail.com", "Thomas", "Ek13");

        userDAO.create(user2);
        user2.getUserId();
        assertThat(user2.getUserId(), notNullValue());
        User fetchedUser2 = userDAO.read(user2.getUserId());
        assertThat(fetchedUser2.getUserId(), is(user2.getUserId()));

        //Testing user3
        User user3 = new User("Jon@mail.com", "Jon", "Ek14");

        userDAO.create(user3);
        user3.getUserId();
        assertThat(user3.getUserId(), notNullValue());
        User fetchedUser3 = userDAO.read(user3.getUserId());
        assertThat(fetchedUser3.getUserId(), is(user3.getUserId()));


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

        //Testing generics
        assertThat(fetchedHost1, not(fetchedUser1));

    }

    @Test
    void update() {

    }

    @Test
    void read() {
    }

    @Test
    void delete() {
    }

    @Test
    void readAll() {
    }
}