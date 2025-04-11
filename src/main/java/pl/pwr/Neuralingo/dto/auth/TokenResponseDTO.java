package pl.pwr.Neuralingo.dto.auth;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken
) {
}
