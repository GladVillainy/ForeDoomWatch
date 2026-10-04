package mapper;

import dto.ReferencesDTO;
import entities.Reference;
import java.util.List;

public class ReferenceMapper implements IMapper<Reference,ReferencesDTO, ReferencesDTO> {


    @Override
    public ReferencesDTO toDTO(Reference entity) {
        ReferencesDTO referencesDTO = new ReferencesDTO(
                entity.getReferenceId(),
                entity.getUrl(),
                entity.getSource(),
                entity.getTags()
        );
        return referencesDTO;
    }

    @Override
    public Reference toEntity(ReferencesDTO dto) {
        return Reference.builder()
                .url(dto.url())
                .source(dto.source())
                .tags(dto.tags())
                .build();
    }

    @Override
    public List<ReferencesDTO> toDTOList(List<Reference> entities) {
        return IMapper.super.toDTOList(entities);
    }
}
