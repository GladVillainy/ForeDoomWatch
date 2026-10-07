package mapper;

import dto.FindingDTO;
import entities.Finding;

import java.util.List;

public class FindingMapper implements IMapper<Finding, FindingDTO, FindingDTO> {
    @Override
    public FindingDTO toDTO(Finding entity) {
        FindingDTO findingDTO = new FindingDTO(
                entity.getFindingId(),
                entity.getStatus(),
                entity.getDetectedAt(),
                entity.getUpdatedAt(),
                entity.getResolvedAt()
        );
        return findingDTO;
    }

    @Override
    public Finding toEntity(FindingDTO dto) {
        return Finding.builder()
                .status(dto.status())
                .build();
    }

    @Override
    public List<FindingDTO> toDTOList(List<Finding> entities) {
        return IMapper.super.toDTOList(entities);
    }
}
