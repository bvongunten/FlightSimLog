package ch.nostromo.flightsimlog.statistics;

import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.fxui.dialogs.TextMessageDialog;

import java.util.*;

public class Statistics {

    public static void createStatistics(List<Flight> flights) {

        // Get all planes in those flights
        Map<SimAircraft, AircraftStatistics> aircraftStatisticsMap = new HashMap<>();

        for (Flight flight : flights) {
            if (!aircraftStatisticsMap.containsKey(flight.getSimAircraft())) {
                aircraftStatisticsMap.put(flight.getSimAircraft(), new AircraftStatistics());
            }

            AircraftStatistics statistics = aircraftStatisticsMap.get(flight.getSimAircraft());
            statistics.simAircraft = flight.getSimAircraft();
            statistics.flightCount++;
            statistics.totalDistance += flight.getCalculatedDistanceInNm();
            statistics.totalSeconds += flight.getCalculatedDurationInSec();
        }

        List<AircraftStatistics> aircraftStatisticsList = new ArrayList<>(aircraftStatisticsMap.values());

        aircraftStatisticsList.sort((o1, o2) -> Integer.compare(o2.getFlightCount(), o1.getFlightCount()));

        StringBuilder sb = new StringBuilder();
        for (AircraftStatistics statistics : aircraftStatisticsList) {
            sb.append(statistics.getSimAircraft() + ", " + statistics.getFlightCount() + ", " + Math.round(statistics.getTotalDistance()) + ", " + Math.round(statistics.getTotalSeconds()) + System.lineSeparator());
        }

        new TextMessageDialog(null, "Statistics", sb.toString()).showAndWait();

    }

}
