package dao;

import entities.Finding;
import entities.FindingStatus;
import exceptions.DatabaseException;
import exceptions.MissingInputException;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.Comparator;
import java.util.List;

/**
 * Data access object(DAO) for the entities of type {@link Finding}
 * FindingDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
 * methods from sup{@link GenericDAO}
 * The DAO contains 2 custom methods: {@link sortByStatusAscending} and {@link sortByStatusDescending}
 */
public class FindingDAO extends GenericDAO<Finding,Long> {
    public FindingDAO(EntityManagerFactory emf) {
        super(emf, Finding.class);
    }


    /**
     * Retrieves all findings that belongings to the given user, sorted by finding status based on enums field order (int)
     *  * {@link FindingStatus} (OPEN = 1, IN_PROGRESS = 2, RESOLVED = 3).
     * @param userId the id of the user who owns the findings
     * @return a list of the users findings sorted by status, empty if none exist
     * @throws DatabaseException if the database query fails (status code 500)
     * @throws MissingInputException if user id is null (400)
     */
    public List<Finding> sortByStatusAscending(Long userId) {
        MissingInputException.requireValue(userId, "id", "User");
        // Åbn en EntityManager med try-with-resources
        try(EntityManager entityManager = emf.createEntityManager()){
            //sorterer efter jpql status
            String JPQL = "SELECT f FROM Finding f" +
                    " WHERE f.software.host.user.userId = :userId";
            try {
                // Opret en TypedQuery<Finding>
                TypedQuery<Finding> query = entityManager.createQuery(JPQL, Finding.class);
                //Set userid som param
                query.setParameter("userId", userId);

                List<Finding> findings = query.getResultList();

                //Sorts enum
                return findings.stream()
                        .sorted(Comparator.comparing(f -> f.getStatus().getOrder()))
                        .toList();

            } catch (PersistenceException e) {
                // Fang PersistenceException og kast med 500
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Getting status has failed with error message " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves all findings that belong to the given user, sorted by status in reverse order. Uses {@link sortByStatusAscending}
     * of the order field in {@link FindingStatus} (RESOLVED = 3, IN_PROGRESS = 2, OPEN = 1).
     * @param userId the id of the user who owns the findings
     * @return a list of the user's findings sorted by status in descending order, empty if none exist
     * @throws MissingInputException if userId is null (status code 400)
     * @throws DatabaseException if the database query fails (status code 500)
     */
    public List<Finding> sortByStatusDescending(Long userId){
        return sortByStatusAscending(userId).reversed();
    }

    public List<Finding> sortCvssAscending(Long userId){
        MissingInputException.requireValue(userId, "id", "User");

        try(EntityManager entityManager = emf.createEntityManager()){
            //sorterer efter jpql status
            String JPQL = "SELECT f FROM Finding f" +
                    " WHERE f.software.host.user.userId = :userId";
            try {
                // Opret en TypedQuery<Finding>
                TypedQuery<Finding> query = entityManager.createQuery(JPQL, Finding.class);
                //Set userid som param
                query.setParameter("userId", userId);

                List<Finding> findings = query.getResultList();

                //Sorts
                return findings.stream()
                        .sorted(Comparator.comparing(f -> f.getVulnerability().getMetrics().getCvssScore()))
                        .toList();

            } catch (PersistenceException e) {
                // Fang PersistenceException og kast med 500
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Getting Cvss has failed with error message " + e.getMessage());
            }
        }

    }

    public List<Finding> sortCvssDescending(Long userId){
        return sortCvssAscending(userId).reversed();
    }


}
