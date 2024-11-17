package ch.nostromo.flightsimlog.data.base;

public enum AircraftPropulsion {
    JET("Jet"),
    PROPELLER("Propeller"),
    TURBOPROP("Turbo Prop"),
    ROTARY_WING("Rotary Wing"),
    ELECTRIC("Electric"),
    UNPOWERED("Unpowered"),
    OTHER("Other");

    private final String displayName;

    AircraftPropulsion(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
