package ch.nostromo.flightsimlog.data.base;

public enum AircraftType {
    AIRLINER("Airliner / Cargo"),
    BUSINESS_JET("Business Jet"),
    GA_SINGLE("General Aviation (Single-Engine)"),
    GA_MULTI("General Aviation (Multi-Engine)"),
    MULTI_PURPOSE("Multi Purpose / Utility"),
    MILITARY("Military"),
    HISTORICAL("Historical / Vintage"),
    HELICOPTER("Helicopter"),
    GLIDER("Glider / Ultralight"),
    LIGHTER_THAN_AIR("Lighter Than Air"),
    FICTIONAL("Fictional"),
    EXPERIMENTAL_DIVERS ("Experimental / Divers");

    private final String displayName;

    AircraftType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
