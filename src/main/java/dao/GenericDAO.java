package dao;

import config.HibernateConfig;
import exceptions.ApiException;
import jakarta.persistence.*;

import java.util.List;

public class GenericDAO<T, ID> {
    private final EntityManagerFactory emf;
    private Class<T> entityClass;

    public GenericDAO(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }

    public GenericDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }
    //uses finally to insure a close, if an exception occurs.

    public T create(T t){
        if (t == null) {
            throw new ApiException(400, entityClass.getSimpleName() + " is required");
        }
        try(EntityManager entityManager = emf.createEntityManager();) {
            entityManager.getTransaction().begin();
            try {
                entityManager.persist(t);
                entityManager.getTransaction().commit();
            } catch (PersistenceException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(500, "Create " + entityClass.getSimpleName()
                        + " failed with error message: " + e.getMessage());
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                } throw new RuntimeException(e);
            }
        }
        return t;
    }

    public T update(T t) {
        if (t == null){
            throw new ApiException(400, entityClass.getSimpleName() + " id is required");
        }
        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            //try { question for teacher, how to find generic id?
              //  T existing = entityManager.find(T.class, T.getId());
              //  if (existing == null) {
              //      throw new ApiException(404, "Study not found");
              //  }
            try {
                T merged = entityManager.merge(t);
                entityManager.getTransaction().commit();
                return merged;
            } catch (PersistenceException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(500, "Update " + entityClass.getSimpleName()
                        + " failed with error message: " + e.getMessage());
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                } throw new RuntimeException(e);
            }
        }
    }

    public T read(ID id) {
        if (id == null){
            throw new ApiException(400, entityClass.getSimpleName() + " id is required");
        }
       try(EntityManager entityManager = emf.createEntityManager()){
           //Find entity
           T t = entityManager.find(entityClass, id);
           if(t != null){
               return t;
           } throw new ApiException(404, entityClass.getSimpleName() + "could not be found");
       } catch (PersistenceException e) {
            throw new ApiException(500, "Failed to fetch with error message: " + e.getMessage());
        }
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
