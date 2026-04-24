package es.ual.dra.autodiagnostico.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.ual.dra.autodiagnostico.dto.AuthLoginRequestDTO;
import es.ual.dra.autodiagnostico.dto.AuthRegisterRequestDTO;
import es.ual.dra.autodiagnostico.dto.AuthUserResponseDTO;
import es.ual.dra.autodiagnostico.model.entitity.user.AppUser;
import es.ual.dra.autodiagnostico.model.entitity.user.UserRole;
import es.ual.dra.autodiagnostico.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthUserResponseDTO register(AuthRegisterRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        UserRole role = UserRole.fromValue(request.getRole());
        String fullName = request.getFullName().trim();
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setAvatarUrl(buildAvatarUrl(fullName, role));
        user.setCreatedAt(LocalDateTime.now());

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public AuthUserResponseDTO login(AuthLoginRequestDTO request) {
        String email = normalizeEmail(request.getEmail());
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        return mapToResponse(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildAvatarUrl(String fullName, UserRole role) {
        String seed = URLEncoder.encode(fullName.isBlank() ? role.name() : fullName, StandardCharsets.UTF_8);
        return "https://api.dicebear.com/9.x/initials/svg?seed=" + seed + "&backgroundColor=1a6bbd";
    }

    private AuthUserResponseDTO mapToResponse(AppUser user) {
        return AuthUserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
