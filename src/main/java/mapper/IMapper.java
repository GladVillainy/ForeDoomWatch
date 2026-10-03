package mapper;

import java.util.List;

/**
 * A generic interface used for mappers, converting between an entity and its DTO representations.
 * The interface distinguishes between outgoing and incoming DTOs, so sensitive or unnecessary
 * information is not exposed
 * @param <E> the entity type
 * @param <Out> the outgoing DTO type
 * @param <In> the incoming DTO type
 */
public interface IMapper<E, Out, In> {
    /**
     * Converts an entity (E) to its outgoing DTO (Out)
     * @param entity the entity to convert
     * @return a DTO containing the data from the given entity
     */
    Out toDTO(E entity);

    /**
     * Converts an incoming DTO(In) to its corresponding entity(E)
     * @param dto the incoming DTO(In) that holds the data sent by the client
     * @return an entity(E) based on the DTO's data
     */
    E toEntity(In dto);

    /**
     * Converts a list of entities (E) to a list of outgoing DTOs (Out).
     * Uses {@link #toDTO(Object)} on each entity.
     * @param entities list of the entities(E) to convert
     * @return a list of outgoing DTOs(Out)
     */
    default List<Out> toDTOList(List<E> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}
