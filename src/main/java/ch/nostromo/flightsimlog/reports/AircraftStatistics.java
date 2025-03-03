package ch.nostromo.flightsimlog.reports;

import ch.nostromo.flightsimlog.data.base.SimAircraft;
import lombok.Data;

@Data
public class AircraftStatistics {

    SimAircraft simAircraft;

    int flightCount;

    double totalDistance;

    double totalSeconds;

}
