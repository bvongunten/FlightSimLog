package ch.nostromo.flightsimlog.data.base;

public enum AircraftSeatingType {
    SOLO("Solo"),
    TWO_SIDE_BY_SIDE("Side by Side (2)"),
    TWO_TANDEM("Tandem (2)"),
    FOUR_BLOCK("Block (4)"),
    PASSENGERS("Pilots & Passengers"),
    OTHER("Other");

    private final String displayName;

    AircraftSeatingType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
