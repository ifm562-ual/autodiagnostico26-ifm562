package es.ual.dra.autodiagnostico.service;

import es.ual.dra.autodiagnostico.dto.AuthLoginRequestDTO;
import es.ual.dra.autodiagnostico.dto.AuthRegisterRequestDTO;
import es.ual.dra.autodiagnostico.dto.AuthUserResponseDTO;

public interface AuthService {

    AuthUserResponseDTO register(AuthRegisterRequestDTO request);

    AuthUserResponseDTO login(AuthLoginRequestDTO request);
}
