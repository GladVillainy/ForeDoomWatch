package dao;

import entities.Software;
import exceptions.DatabaseException;
import exceptions.MissingInputException;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 * Data access object(DAO) for the entities of type {@link Software}
 * SoftwareDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
 * methods from sup {@link GenericDAO}
 * The DAO contains 1 custom method: {@link #readByHost(Long)}, which retrieves all software running on a given host
 */
public class SoftwareDAO extends GenericDAO<Software, Long> {

    public SoftwareDAO(EntityManagerFactory emf, Class<Software> entityClass) {
        super(emf, Software.class);
    }


    /**
     * Retrieves all software that belongs to the given host.
     *
     * @param hostId the id of the host that contains the software
     * @return a list of the host's software, empty if none exist
     * @throws MissingInputException if hostId is null (400)
     * @throws DatabaseException     if the database query fails (status code 500)
     */
    public List<Software> readByHost(Long hostId) {
        // Tjek at hostId ikke er null
        MissingInputException.requireValue(hostId, "id", "Host");
        // EntityManager med try-with-resources
        try (EntityManager entityManager = emf.createEntityManager()) {
            // Hent software hvor hosten har det givne id
            String jpql = "SELECT s FROM Software s WHERE s.host.hostId = :hostId";
            try {
                // Opret en TypedQuery<Software> og sæt param hostId
                TypedQuery<Software> query = entityManager.createQuery(jpql, Software.class);
                query.setParameter("hostId", hostId);
                return query.getResultList();
            } catch (PersistenceException e) {
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Getting software failed with error: " + e.getMessage());
            }
        }
    }
}
