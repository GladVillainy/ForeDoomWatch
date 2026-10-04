package dto;

import java.util.List;

public record ReferencesDTO(
        Long referenceId,
        String url,
        String source,
        List<String> tags
) {
}
