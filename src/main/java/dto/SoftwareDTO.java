package dto;

public record SoftwareDTO(
      Long softwareId,
      String softwareName,
      String version,
      String vendor
) {
}
