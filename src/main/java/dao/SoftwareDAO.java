package dao;

import entities.Software;
import jakarta.persistence.EntityManagerFactory;

public class SoftwareDAO extends GenericDAO<Software, Long>  {

    public SoftwareDAO(EntityManagerFactory emf, Class<Software> entityClass) {
        super(emf, Software.class);
    }
}
