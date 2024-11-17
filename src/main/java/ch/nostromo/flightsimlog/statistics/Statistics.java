package ch.nostromo.flightsimlog.statistics;

import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.flight.Flight;

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
            statistics.totalDistance += flight.getDistanceNm();
            statistics.totalSeconds += flight.getAirSeconds();
        }

        List<AircraftStatistics> aircraftStatisticsList = new ArrayList<>(aircraftStatisticsMap.values());

        aircraftStatisticsList.sort((o1, o2) -> Integer.compare(o2.getFlightCount(), o1.getFlightCount()));


        for (AircraftStatistics statistics : aircraftStatisticsList) {
            System.out.println(statistics.getSimAircraft() + ", " + statistics.getFlightCount() + ", " + Math.round(statistics.getTotalDistance()) + ", " + Math.round(statistics.getTotalSeconds()));
        }


    }

}
