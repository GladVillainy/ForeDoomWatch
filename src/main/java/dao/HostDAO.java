    package dao;

    import entities.Finding;
    import entities.Host;
    import jakarta.persistence.EntityManagerFactory;

    /**
     * Data access object(DAO) for the entities of type {@link Host}
     * HostDAO extends {@link GenericDAO}, therefor inherits the same CRUD (Create, Read, Update, Delete)
     * methods from sup {@link GenericDAO}
     */
    public class HostDAO extends GenericDAO<Host, Long> {
        public HostDAO(EntityManagerFactory emf) {
            super(emf, Host.class);
        }
    }
