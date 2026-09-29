package utils;

import dao.FindingDAO;
import dao.SoftwareDAO;
import dao.VulnerabilityDAO;
import dto.NVDDTO;
import dto.VulnerabilitiesDTO;
import entities.Finding;
import entities.Software;
import entities.Vulnerability;
import mapper.VulnerabilityMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Service {
    private APIUtils apiUtils;
    private VulnerabilityMapper mapper;
    private  SoftwareDAO softwareDAO;
    private  VulnerabilityDAO vulnerabilityDAO;
    private  FindingDAO findingDAO;

    String apiKey = System.getenv("apiKey");

    public List<Finding> findVulnerabilities(Long softwareId) {
        //Hent softwaren fra databasen og brug readAPI
       Software found = softwareDAO.read(softwareId);
       String json = apiUtils.readAPI(apiKey, found.getVendor(), found.getSoftwareName(), found.getVersion());

        //Parse JSON til NVDDTOen
       NVDDTO nvddto = apiUtils.convertFromJson(json);

        //Map til en Vulnerability
        List<Vulnerability> vulnerabilities = nvddto.vulnerabilities()
                .stream()
                .map(VulnerabilitiesDTO::cve)
                .map(mapper::mapToVulnerability)
                .toList();

        //Gem det findings i en liste
        List<Finding> findings = new ArrayList<>();

        // Gem hver Vulnerability
        for (Vulnerability v : vulnerabilities) {
            vulnerabilityDAO.create(v);

            // For hver Vulnerability: lav et Finding, der peger på softwaren og vulnerabilityen og gem
           Finding finding = Finding.builder()
                    .detectedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .software(found)
                    .vulnerability(v)
                    .build();
            findingDAO.create(finding);

            findings.add(finding);
        }
        return findings;
    }
}