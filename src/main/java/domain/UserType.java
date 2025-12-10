package domain;

public enum UserType {
    DRIVER,     // Solo puede crear viajes
    TRAVELER,   // Solo puede reservar
    BOTH        // Puede hacer las dos cosas
}