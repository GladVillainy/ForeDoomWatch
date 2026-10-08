package mapper;

import dto.FindingDTO;
import dto.FindingDetailDTO;
import entities.Finding;
import entities.Host;
import entities.Software;
import entities.Vulnerability;

import java.util.ArrayList;
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

    /**
     * Maps a finding to a FindingDetailDTO, which also contains information from the
     * vulnerability, software and host the finding belongs to.
     * @param finding the finding to convert
     * @return a FindingDetailDTO. cvssScore is null if the CVE has no CVSS score
     */
    public FindingDetailDTO toDetailDTO(Finding finding) {
        //Hent de tilknyttede entities
        Vulnerability vulnerability = finding.getVulnerability();
        Software software = finding.getSoftware();
        Host host = software.getHost();

        //Handle nullpointexeptions
        Double cvssScore = null;
        if (vulnerability.getMetrics() != null) {
            cvssScore = vulnerability.getMetrics().getCvssScore();
        }

        String hostName = null;
        if (host != null) {
            hostName = host.getHostName();
        }

        FindingDetailDTO findingDetailDTO = new FindingDetailDTO(
                finding.getFindingId(),
                finding.getStatus(),
                finding.getDetectedAt(),
                vulnerability.getVulnerabilityId(),
                cvssScore,
                software.getSoftwareName(),
                software.getVersion(),
                hostName
        );
        return findingDetailDTO;
    }

    public List<FindingDetailDTO> toDetailDTOList(List<Finding> findings) {
        //mapper hver finding til en FindingDetailDTO
        return findings.stream()
                .map(this::toDetailDTO)
                .toList();
    }
}