package es.ual.dra.autodiagnostico.model.entitity.user;

public enum UserRole {
    USER,
    TALLER,
    ADMIN;

    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        return UserRole.valueOf(value.trim().toUpperCase());
    }
}
