package dao;

import entities.Finding;
import jakarta.persistence.EntityManagerFactory;

public class FindingDAO extends GenericDAO<Finding,Long> {
    public FindingDAO(EntityManagerFactory emf, Class<Finding> entityClass) {
        super(emf, entityClass);
    }
}
