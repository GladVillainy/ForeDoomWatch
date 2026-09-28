package entities;

/**
 * Essential for GenericDAO to function.
 * It promises the DAO that every entity has an ID, so the DAO can find
 * the existing entity in update() without knowing the name of the id field.
 *
 * @param <ID> the type of the id, Long for most entities.
 */
public interface IEntity<ID> {
    ID getID();
}
