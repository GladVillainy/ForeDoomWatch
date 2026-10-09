package security.dao;

import dao.GenericDAO;
import security.entities.Roles;
import security.entities.User;
import exceptions.ApiException;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data access object(DAO) for the entities of type {@link User}
 * UserDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
 * methods from the sup {@link GenericDAO}
 * UserDAO also implements {@link ISecurityDAO}, which is used for register and login
 */
public class UserDAO extends GenericDAO<User, Long> implements ISecurityDAO {
    public UserDAO(EntityManagerFactory emf) {
        super(emf, User.class);
    }

    /**
     * Finds a user by email.
     * @param email the email to search for
     * @return the user, or null if no user has that email
     * @throws ValidationException if email is null or blank (status code 400)
     * @throws DatabaseException if the database query fails (status code 500)
     */
    @Override
    public User findByEmail(String email) {
        ValidationException.requireValue(email, "email", "User");
        try (EntityManager entityManager = emf.createEntityManager()) {

            String JPQL = "SELECT u FROM User u WHERE u.email = :email";

            try {
                // Opret en TypedQuery<User>
                TypedQuery<User> query = entityManager.createQuery(JPQL, User.class);
                //Set email som param
                query.setParameter("email", email);

                List<User> userList = query.getResultList();

                //Forhindre null point exception
                if (userList.isEmpty()) {
                    return null;
                }
                return userList.getFirst();
            } catch (PersistenceException e) {
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Getting user has failed with error message " + e.getMessage());
            }
        }
    }

    @Override
    public User getVerifiedUser(String email, String password) {
        User user = findByEmail(email);

        if (user == null || !user.verifyPassword(password)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        return user;
    }

    @Override
    public User createUser(String username, String email, String password) {
        ValidationException.validateEmail(email);
        ValidationException.requireValue(password, "password", "User");

        //Emailen skal være unik
        if (findByEmail(email) != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Email has to be unique");
        }

        //Konstruktøren hasher passwordet
        User newUser = new User(username, email, password);
        newUser.addRole(Roles.USER);


        return create(newUser);
    }

    @Override
    public User addUserRole(String email, Roles role) {
        User user = findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User could not be found");
        }
        user.addRole(role);

        //Gem ændringen
        return update(user);
    }
}