package dao;

import entities.Finding;
import entities.FindingStatus;
import exceptions.DatabaseException;
import exceptions.ValidationException;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.Comparator;
import java.util.List;

/**
 * Data access object(DAO) for the entities of type {@link Finding}.
 * FindingDAO extends {@link GenericDAO}, therefore inherits the same CRUD (Create, Read, Update, Delete)
 * methods from {@link GenericDAO}.
 * The DAO contains custom methods for sorting a user's findings by status or CVSS score:
 * {@link #sortByStatusAscending(Long)}, {@link #sortByStatusDescending(Long)},
 * {@link #sortCvssAscending(Long)} and {@link #sortCvssDescending(Long)},
 * and {@link #doesItExist(Long, String)} to check for duplicates.
 */
public class FindingDAO extends GenericDAO<Finding, Long> {
    public FindingDAO(EntityManagerFactory emf) {
        super(emf, Finding.class);
    }


    /**
     * Retrieves all findings that belong to the given user, sorted by finding status based on the enum's
     * order field {@link FindingStatus} (OPEN = 1, IN_PROGRESS = 2, RESOLVED = 3).
     *
     * @param userId the id of the user who owns the findings
     * @return a list of the users findings sorted by status, empty if none exist
     * @throws DatabaseException   if the database query fails (status code 500)
     * @throws ValidationException if user id is null (400)
     */
    public List<Finding> sortByStatusAscending(Long userId) {
        ValidationException.requireValue(userId, "user", "id");
        // Åbn en EntityManager med try-with-resources
        try (EntityManager entityManager = emf.createEntityManager()) {
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
     * Retrieves all findings that belong to the given user, sorted by status in reverse order
     * (RESOLVED, IN_PROGRESS, OPEN). Uses {@link #sortByStatusAscending(Long)}.
     *
     * @param userId the id of the user who owns the findings
     * @return a list of the user's findings sorted by status in descending order, empty if none exist
     * @throws ValidationException if userId is null (status code 400)
     * @throws DatabaseException   if the database query fails (status code 500)
     */
    public List<Finding> sortByStatusDescending(Long userId) {
        return sortByStatusAscending(userId).reversed();
    }

    /**
     * Retrieves all findings that belong to the given user, sorted by cvss score in ascending order.
     * Findings without a cvss score are placed last. Uses {@link #sortCvss(Long, Comparator)}.
     *
     * @param userId the id of the user who owns the findings
     * @return a list of the user's findings sorted by cvss score in ascending order, empty if none exist
     * @throws ValidationException if userId is null (status code 400)
     * @throws DatabaseException   if the database query fails (status code 500)
     */
    public List<Finding> sortCvssAscending(Long userId) {
        return sortCvss(userId, Comparator.naturalOrder());
    }

    /**
     * Retrieves all findings that belong to the given user, sorted by cvss score in descending order.
     * Findings without a cvss score are placed last. Uses {@link #sortCvss(Long, Comparator)}.
     *
     * @param userId the id of the user who owns the findings
     * @return a list of the user's findings sorted by cvss score in descending order, empty if none exist
     * @throws ValidationException if userId is null (status code 400)
     * @throws DatabaseException   if the database query fails (status code 500)
     */
    public List<Finding> sortCvssDescending(Long userId) {
        return sortCvss(userId, Comparator.reverseOrder());
    }

    /**
     * Retrieves findings for the given user and sorts them by CVSS score using the given comparator.
     * Uses {@link #cvssScoreOrNull(Finding)} to prevent {@link NullPointerException} when sorting.
     *
     * @param userId the id of the user who owns the findings
     * @param order  how the cvss needs to be sorted
     * @return a list of sorted cvss based on the param order
     * @throws ValidationException if userid is null (status code 400)
     * @throws DatabaseException   if the query fails to fetch (status code 500)
     */
    private List<Finding> sortCvss(Long userId, Comparator<Double> order) {
        ValidationException.requireValue(userId, "id", "User");

        try (EntityManager entityManager = emf.createEntityManager()) {
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
                        .sorted(Comparator.comparing(this::cvssScoreOrNull, Comparator.nullsLast(order)))
                        .toList();
            } catch (PersistenceException e) {
                // Fang PersistenceException og kast med 500
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Getting Cvss has failed with error message " + e.getMessage());
            }
        }

    }

    /**
     * Helper method used in {@link #sortCvss(Long, Comparator)} to prevent a {@link NullPointerException}
     * when sorting.
     *
     * @param finding the finding to read the CVSS score from
     * @return the cvss score, or null if the vulnerability or its metrics are missing
     */
    private Double cvssScoreOrNull(Finding finding) {
        if (finding.getVulnerability() == null) {
            return null;
        } else if (finding.getVulnerability().getMetrics() == null) {
            return null;
        }
        return finding.getVulnerability().getMetrics().getCvssScore();
    }

    /**
     * Check whether a finding already exists
     *
     * @param softwareId id of the software
     * @param vulnerabilityId the cve id from vulnerability
     * @return true if a finding for this software and vulnerability already exists, otherwise false
     * @throws ValidationException if softwareId or vulnerabilityId is null (status code 400)
     * @throws DatabaseException   if the database query fails (status code 500)
     */
    public boolean doesItExist(Long softwareId, String vulnerabilityId) {
        ValidationException.requireValue(softwareId, "id", "software");
        ValidationException.requireValue(vulnerabilityId, "id", "vulnerability");

        try (EntityManager entityManager = emf.createEntityManager()) {
            String jpql = "SELECT count(f) from Finding  f where f.software.softwareId = " +
                    ":softwareId and f.vulnerability.vulnerabilityId = :vulnerabilityId";

            try {
                TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                query.setParameter("softwareId", softwareId);
                query.setParameter("vulnerabilityId", vulnerabilityId);

                return query.getSingleResult() > 0;
            } catch (Exception e) {
                throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Fetching data failed with error message: " + e);
            }
        }

    }
}
