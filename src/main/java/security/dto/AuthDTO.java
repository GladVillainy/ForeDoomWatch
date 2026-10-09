package security.dto;

public record AuthDTO(
     String username,
     String email,
     String password
){ }