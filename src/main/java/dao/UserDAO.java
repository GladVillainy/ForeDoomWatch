package dao;

import entities.Finding;
import entities.User;
import jakarta.persistence.EntityManagerFactory;

/**
 * Data access object(DAO) for the entities of type {@link User}
 * UserDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
 * methods from the sup {@link GenericDAO}
 */
public class UserDAO extends GenericDAO<User, Long> {
    public UserDAO(EntityManagerFactory emf) {
        super(emf, User.class);
    }
}
