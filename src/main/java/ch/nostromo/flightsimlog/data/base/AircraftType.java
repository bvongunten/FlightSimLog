package ch.nostromo.flightsimlog.data.base;

public enum AircraftType {
    AIRLINER("Airliner"),
    GENERAL_AVIATION("General Aviation"),
    MILITARY("Military"),
    AMPHIBIOUS("Amphibious"),
    GLIDER("Glider"),
    AEROBATIC("Aerobatic"),
    HELICOPTER("Helicopter"),
    EXPERIMENTAL("Experimental"),
    FICTIONAL("Fictional"),
    OTHER  ("Other");

    private final String displayName;

    AircraftType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
