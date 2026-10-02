package mapper;

import java.util.List;

public interface IMapper<E, D, R> {
    D toDTO(E entity);
    E toEntity(R dto);

    default List<D> toDTOList(List<E> entities) {
        return entities.stream()
                .map(this::toDTO)
                .toList();
    }
}
