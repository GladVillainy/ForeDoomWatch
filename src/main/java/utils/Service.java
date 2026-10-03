package utils;

import dao.FindingDAO;
import dao.SoftwareDAO;
import dao.VulnerabilityDAO;
import dto.nvd.NVDDTO;
import dto.nvd.NVDVulnerabilitiesDTO;
import entities.Finding;
import entities.Software;
import entities.Vulnerability;
import exceptions.MissingInputException;
import mapper.NVDMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Service {
    private APIUtils apiUtils;
    private NVDMapper mapper;
    private  SoftwareDAO softwareDAO;
    private  VulnerabilityDAO vulnerabilityDAO;
    private  FindingDAO findingDAO;

    String apiKey = System.getenv("apiKey");


    /**
     * Checks a software against NVD api and creates a finding for every known vulnerability.
     * The vulnerabilities and findings are saved in the database using DAO.
     * @param softwareId the id of the software to check
     * @return a list of the new findings, or an empty list if nothing was found
     * @throws MissingInputException if vendor, name or version on the software is null or blank
     * @throws exceptions.ApiException if the software does not exist (404) or NVD could not be reached or read (429, 502, 503)
     */
    public List<Finding> findVulnerabilities(Long softwareId) {
        MissingInputException.requireValue(softwareId, "ID", "Software");

        //Hent softwaren fra databasen og brug readAPI
        Software found = softwareDAO.read(softwareId);

        //Error handling hvis værdier er empty
        MissingInputException.requireValue(found.getVendor(), "vendor", "Software");
        MissingInputException.requireValue(found.getSoftwareName(), "name", "Software");
        MissingInputException.requireValue(found.getVersion(), "version", "Software");

       String json = apiUtils.readAPI(apiKey, found.getVendor(), found.getSoftwareName(), found.getVersion());

        //Parse JSON til NVDDTOen
       NVDDTO nvddto = apiUtils.convertFromJson(json);

        //Map til en Vulnerability
        List<Vulnerability> vulnerabilities = nvddto.vulnerabilities()
                .stream()
                .map(NVDVulnerabilitiesDTO::cve)
                .map(mapper::vulnerabilityToEntity)
                .toList();

        //Gem det findings i en liste
        List<Finding> findings = new ArrayList<>();

        // Gem hver Vulnerability
        for (Vulnerability v : vulnerabilities) {
            vulnerabilityDAO.create(v);

            // For hver Vulnerability lav et Finding, der peger på softwaren og vulnerabilityen og gem
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