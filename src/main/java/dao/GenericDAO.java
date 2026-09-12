package dao;

import config.HibernateConfig;
import exceptions.ApiException;
import jakarta.persistence.*;

import java.util.List;

public class GenericDAO<T, ID> {
    private EntityManagerFactory emf;
    private Class<T> entityClass;

    public GenericDAO(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }

    public GenericDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public GenericDAO() {}
    //uses finally to insure a close, if an exception occurs.

    public T create(T t){
        EntityManager entityManager = emf.createEntityManager();

        entityManager.getTransaction().begin();
        try{
            entityManager.persist(t);
            entityManager.getTransaction().commit();
            return t;
        } finally {
            entityManager.close();
        }
    }

    public T update(T t) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T merged = entityManager.merge(t);
            entityManager.getTransaction().commit();
            return merged;
        } finally {
            entityManager.close();
        }
    }

    public T read(ID id) {
        EntityManager entityManager = emf.createEntityManager();
        //Find entity
        try {
            return entityManager.find(entityClass, id);
        }
        catch (PersistenceException e) {
            throw new ApiException(500, "Failed to fetch" + e.getMessage());
        }
        //Close entityManger
        finally {entityManager.close();}

    }

    public void delete(ID id){
        EntityManager entityManager = emf.createEntityManager();

        entityManager.getTransaction().begin();

        //Finds entity based on id
        try {
            T foundEntity = entityManager.find(entityClass, id);
            //checks if its empty
            if (foundEntity != null) {
                entityManager.remove(foundEntity);
            }
            entityManager.getTransaction().commit();
        }finally {
            //closes
            entityManager.close();
        }
    }

    public List<T> readAll(){
        EntityManager entityManager = emf.createEntityManager();

        //Select all from class. Simplename converts to string
        String JPQL = "SELECT t FROM " + entityClass.getSimpleName() + " t";

        try {
            TypedQuery<T> query =
                    entityManager.createQuery(JPQL, entityClass);
            List<T> entities = query.getResultList();
            return entities;
        } finally {
            entityManager.close();
        }
    }
}
