    package dao;

    import entities.Host;
    import jakarta.persistence.EntityManagerFactory;

    public class HostDAO extends GenericDAO<Host, Long> {
        public HostDAO(EntityManagerFactory emf) {
            super(emf, Host.class);
        }
    }
