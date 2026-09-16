package dao;


import entities.IEntity;
import exceptions.ApiException;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;

import java.util.List;

public class GenericDAO<T extends IEntity, ID> {
    private final EntityManagerFactory emf;
    private Class<T> entityClass;

    public GenericDAO(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }

    public T create(T t) {
        //handles if T is missing
        if (t == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), entityClass.getSimpleName() + " is required");
        }
        try (EntityManager entityManager = emf.createEntityManager();) {
            entityManager.getTransaction().begin();
            try {
                entityManager.persist(t);
                entityManager.getTransaction().commit();
            } catch (PersistenceException e) {
                //Rollback to prevent damage to database
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Create " + entityClass.getSimpleName()
                        + " failed with error message: " + e.getMessage());
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
        return t;
    }

    public T update(T t) {
        //handles if t is missing
        if (t == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), entityClass.getSimpleName() + " id is required");
        }
        T merged = null;
        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();

            try {
                //If id is null
                T existing = entityManager.find(entityClass, t.getID());
                if (existing == null) {
                    throw new ApiException(HttpStatus.NOT_FOUND.value(), entityClass.getSimpleName() + "' ID could not be found");
                }try {
                    merged = entityManager.merge(t);
                    entityManager.getTransaction().commit();
                } catch (PersistenceException e) {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                    throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Update " + entityClass.getSimpleName()
                            + " failed with error message: " + e.getMessage());
                } catch (RuntimeException e) {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return merged;
    }

    public T read(ID id) {
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), entityClass.getSimpleName() + " id is required");
        }
        try (EntityManager entityManager = emf.createEntityManager()) {
            //Find entity
            T t = entityManager.find(entityClass, id);
            if (t != null) {
                return t;
            }
            throw new ApiException(HttpStatus.NOT_FOUND.value(), entityClass.getSimpleName() + " could not be found");
        } catch (PersistenceException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch with error message: " + e.getMessage());
        }
    }

    public boolean delete(ID id) {
        //ID check
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), entityClass.getSimpleName() + " id is required.");
        }

        Boolean isDeleted = false;

        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            try {
                //Finds entity based on id
                T foundEntity = entityManager.find(entityClass, id);
                //checks if its empty
                if (foundEntity != null) {
                    entityManager.remove(foundEntity);
                    entityManager.getTransaction().commit();
                    isDeleted = true;
                } else {
                    //Handle if entity could not be found
                    throw new ApiException(HttpStatus.BAD_REQUEST.value(), entityClass.getSimpleName() + " could not be found");
                }
            } catch (PersistenceException e) {
                //Roll back to prevent potential damage to database
                if (entityManager.isOpen()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Deletion of " + entityClass.getSimpleName() +
                        " has failed with error message: " + e.getMessage());
            }
        }
        return isDeleted;
    }

    public List<T> readAll() {
        try (EntityManager entityManager = emf.createEntityManager()) {

            //Select all from class. Simplename converts to string
            String JPQL = "SELECT t FROM " + entityClass.getSimpleName() + " t";

            try {
                TypedQuery<T> query = entityManager.createQuery(JPQL, entityClass);
                List<T> entities = query.getResultList();
                return entities;
            } catch (PersistenceException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Get " + entityClass.getSimpleName()
                        + "has failed with error message" + e.getMessage());
            }
        }
    }
}