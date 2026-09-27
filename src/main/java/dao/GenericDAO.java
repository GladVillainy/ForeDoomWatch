package dao;


import entities.IEntity;
import exceptions.ApiException;
import jakarta.persistence.*;
import io.javalin.http.HttpStatus;

import java.util.List;

public class GenericDAO<T extends IEntity, ID> {
    //Status code overview link:
    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/HttpStatus.html

    private final EntityManagerFactory emf;
    private Class<T> entityClass;

    public GenericDAO(EntityManagerFactory emf, Class<T> entityClass) {
        this.emf = emf;
        this.entityClass = entityClass;
    }


    /**
     * Creates and persists a new entity of type T in the database.
     * @param t the entity to persist
     * @return the persisted entity of type T
     * @throws ApiException if t is null (HTTP status code 400)
     * @throws ApiException if a connection to the database could not be established (HTTP status code 500)
     */
    public T create(T t) {
        //handles if T is missing
        if (t == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.getCode(), entityClass.getSimpleName() + " is required");
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
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Create " + entityClass.getSimpleName()
                        + " failed with error message: " + e.getMessage());
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                } throw e;
            }
        }
        return t;
    }

    /**
     * Updates an existing entity of type T, matched by its ID.
     * @param t the entity containing the updated values, its ID is used to find the existing entity
     * @return the updated (merged) entity of type T
     * @throws ApiException if t is null (HTTP status code 400)
     * @throws ApiException if no entity of type T is found with the given ID (HTTP status code 404)
     * @throws ApiException if a connection to the database could not be established (HTTP status code 500)
     */
    public T update(T t) {
        if (t == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.getCode(),
                    entityClass.getSimpleName() + " id is required");
        }
        T merged = null;
        try (EntityManager entityManager = emf.createEntityManager()) {
            entityManager.getTransaction().begin();
            try {
                T existing = entityManager.find(entityClass, t.getID());
                if (existing == null) {
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(),
                            entityClass.getSimpleName() + " ID could not be found");
                }
                merged = entityManager.merge(t);
                entityManager.getTransaction().commit();
            } catch (PersistenceException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                        "Update " + entityClass.getSimpleName()
                                + " failed with error message: " + e.getMessage());
            } catch (RuntimeException e) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                } throw e;
            }
        }
        return merged;
    }

    /**
     * Finds an entity of type T in the database using the given ID.
     * @param id the ID of the entity to find
     * @return the entity of type T if found
     * @throws ApiException if id is null (HTTP status code 400)
     * @throws ApiException if no entity of type T is found with the given ID (HTTP status code 404)
     * @throws ApiException if a connection to the database could not be established (HTTP status code 500)
     */
    public T read(ID id) {
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.getCode(), entityClass.getSimpleName() + " id is required");
        }
        try (EntityManager entityManager = emf.createEntityManager()) {
            //Find entity
            T t = entityManager.find(entityClass, id);
            if (t != null) {
                return t;
            }
            throw new ApiException(HttpStatus.NOT_FOUND.getCode(), entityClass.getSimpleName() + " could not be found");
        } catch (PersistenceException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Failed to fetch with error message: " + e.getMessage());
        }
    }

    /**
     * Deletes Type T from database using param ID
     * @param id the ID of the entity
     * @return true if deletion was successful, else false
     * @throws ApiException if id is null (Https status code 400)
     * @throws ApiException if it could not find a Type T using ID (Https status code 404)
     * @throws ApiException if it was unable to establishes a connection to database (Https status code 500)
     */
    public boolean delete(ID id) {
        //ID check
        if (id == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST.getCode(), entityClass.getSimpleName() + " id is required.");
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
                    throw new ApiException(HttpStatus.NOT_FOUND.getCode(), entityClass.getSimpleName() + " could not be found");
                }
            } catch (PersistenceException e) {
                //Roll back to prevent potential damage to database
                if (entityManager.isOpen()) {
                    entityManager.getTransaction().rollback();
                }
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Deletion of " + entityClass.getSimpleName() +
                        " has failed with error message: " + e.getMessage());
            }
        }
        return isDeleted;
    }

    /**
     * Retries all entities from type T from the database
     * @return a list of all entities of type T, empty list if none exist.
     * @throws ApiException if the database query fails (status code 500)
     */
    public List<T> readAll() {
        try (EntityManager entityManager = emf.createEntityManager()) {

            //Select all from class. Simplename converts to string
            String JPQL = "SELECT t FROM " + entityClass.getSimpleName() + " t";

            try {
                TypedQuery<T> query = entityManager.createQuery(JPQL, entityClass);
                List<T> entities = query.getResultList();
                return entities;
            } catch (PersistenceException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "Get " + entityClass.getSimpleName()
                        + "has failed with error message" + e.getMessage());
            }
        }
    }
}