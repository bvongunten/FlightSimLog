package ch.nostromo.flightsimlog.data.base;

public enum AircraftGauges {
    STEAM("Steam"),
    HYBRID("Hybrid"),
    DIGITAL("Digital"),
    OTHER("Other");

    private final String displayName;

    AircraftGauges(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
