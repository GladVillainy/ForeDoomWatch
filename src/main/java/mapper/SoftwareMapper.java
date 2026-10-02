package mapper;

import dto.SoftwareDTO;
import entities.Software;

import java.util.List;

public class SoftwareMapper implements IMapper<Software, SoftwareDTO, SoftwareDTO> {
    @Override
    public Software toEntity(SoftwareDTO dto) {
        return Software.builder()
                .softwareName(dto.softwareName())
                .version(dto.version())
                .vendor(dto.vendor())
                .build();
    }

    @Override
    public SoftwareDTO toDTO(Software entity) {
        SoftwareDTO softwareDTO = new SoftwareDTO(
                entity.getID(),
                entity.getSoftwareName(),
                entity.getVersion(),
                entity.getVendor()
        );
        return softwareDTO;
    }

    @Override
    public List<SoftwareDTO> toDTOList(List<Software> entities) {
        return IMapper.super.toDTOList(entities);
    }
}


