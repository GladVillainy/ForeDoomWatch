package mapper;

import dto.HostDTO;
import entities.Host;

import java.util.List;

public class HostMapper implements IMapper<Host, HostDTO, HostDTO> {


    @Override
    public HostDTO toDTO(Host entity) {
        HostDTO hostDTO = new HostDTO(
                entity.getID(),
                entity.getHostName(),
                entity.getHostDescription()
        );
        return hostDTO;
    }

    @Override
    public Host toEntity(HostDTO dto) {
        return Host.builder()
                .hostName(dto.hostname())
                .hostDescription(dto.hostDescription())
                .build();
    }

    @Override
    public List<HostDTO> toDTOList(List<Host> entities) {
        return IMapper.super.toDTOList(entities);
    }
}
