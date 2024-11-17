package ch.nostromo.flightsimlog.data.flight;

public enum FlightSim {
    XP_11("XPlane 11"), XP_12("XPlane 12"), MSFS_2020("MSFS 2020"), MSFS_2024("MSFS 2024l");

    private final String displayName;

    FlightSim(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
