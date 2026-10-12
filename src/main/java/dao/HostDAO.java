    package dao;

    import entities.Host;
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
     * Data access object(DAO) for the entities of type {@link Host}
     * HostDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
     * methods from sup {@link GenericDAO}
     */
    public class HostDAO extends GenericDAO<Host, Long> {
        public HostDAO(EntityManagerFactory emf) {
            super(emf, Host.class);
        }


        public List<Host> findByUserId(Long userId) {
            ValidationException.requireValue(userId, "id", "User");
            try (EntityManager entityManager = emf.createEntityManager()) {
                String jpql = "SELECT h FROM Host h WHERE h.user.userId = :userId";
                try {
                    TypedQuery<Host> query = entityManager.createQuery(jpql, Host.class);
                    query.setParameter("userId", userId);
                    List<Host> resultList = query.getResultList();
                    return resultList;
                } catch (PersistenceException e) {
                    throw new DatabaseException(HttpStatus.INTERNAL_SERVER_ERROR, "Get Host " +
                            "has failed with error message" + e.getMessage());
                }
            }
        }
    }


