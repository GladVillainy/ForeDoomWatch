package dao;

import entities.Finding;
import entities.Software;
import jakarta.persistence.EntityManagerFactory;

/**
 * Data access object(DAO) for the entities of type {@link Software}
 * SoftwareDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
 * methods from sup {@link GenericDAO}
 */
public class SoftwareDAO extends GenericDAO<Software, Long>  {

    public SoftwareDAO(EntityManagerFactory emf, Class<Software> entityClass) {
        super(emf, Software.class);
    }
}
