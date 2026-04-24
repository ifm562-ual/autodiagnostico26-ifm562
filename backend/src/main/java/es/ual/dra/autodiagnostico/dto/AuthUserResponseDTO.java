package es.ual.dra.autodiagnostico.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
