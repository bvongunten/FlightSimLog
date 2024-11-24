package ch.nostromo.flightsimlog.data.base;

public enum AircraftType {
    AIRPLANE("Airplane"),
    HELICOPTER("Helicopter"),
    GLIDER("Glider"),
    LIGHTER_THAN_AIR("Lighter Than Air"),
    FICTIONAL("Fictional"),
    OTHER  ("Other"),
    UNDEF  ("Undef");

    private final String displayName;

    AircraftType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
