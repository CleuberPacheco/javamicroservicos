package br.com.fiap.esg.dto;

public record TokenResponseDTO(
        String token,
        String tipo,
        String email,
        String role
) {
}
