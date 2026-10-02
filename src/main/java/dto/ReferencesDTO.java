package dto;

import java.util.List;

public record ReferencesDTO(
        Long refernceId,
        String url,
        String source,
        List<String> tags
) {
}
