package dao;

import entities.User;
import jakarta.persistence.EntityManagerFactory;


public class UserDAO extends GenericDAO<User, Long> {
    public UserDAO(EntityManagerFactory emf) {
        super(emf, User.class);
    }
}
