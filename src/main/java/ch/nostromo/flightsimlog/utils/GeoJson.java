package ch.nostromo.flightsimlog.utils;


import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.coordinates.Coordinates;
import ch.nostromo.flightsimlog.data.coordinates.WorldPosition;
import ch.nostromo.flightsimlog.data.flight.Flight;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeoJson {

    public static String createGeoJson(Flight flight, boolean includePath) {
        return createGeoJson(new ArrayList<>(List.of(flight)), includePath);
    }

    public static String createGeoJson(List<Flight> flights, boolean includePath) {
        FeatureCollection featureCollection = new FeatureCollection();

        Set<String> visitedPositions = new HashSet<>();

        for (Flight flight : flights) {
            addPosition(featureCollection, visitedPositions, flight.getDeparturePosition());
            addPosition(featureCollection, visitedPositions, flight.getArrivalPosition());

            if (includePath) {
                addPath(featureCollection, flight);
            }

        }

        try {
            return new ObjectMapper().writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            throw new FlightSimLogException("unable to create json", e);
        }
    }


    private static void addPosition(FeatureCollection featureCollection, Set<String> visitedPositions, WorldPosition position) {

        String icao = position.getIcao();
        if (icao == null) {
            icao = "";
        }

        String description = position.getDescription();

        if (visitedPositions.contains(icao)) {
            return;
        }

        visitedPositions.add(icao);
        Feature feature = new Feature();

        Point point = new Point();
        point.setCoordinates(new LngLatAlt(position.getCoordLon(), position.getCoordLat()));
        feature.setGeometry(point);

        if (icao.isEmpty()) {
            feature.setProperty("name", "Free position: " + position.getDescription());
        } else {
            feature.setProperty("name", icao + " - " + description);
        }

        featureCollection.add(feature);

    }

    private static void addPath(FeatureCollection featureCollection, Flight flight) {
        Feature feature = new Feature();
        LineString line = new LineString();

        if (flight.getSimulationData().getMeasurements().size() > 1) {
            for (Coordinates coords : flight.getSimulationData().getMeasurements()) {
                line.getCoordinates().add(new LngLatAlt(coords.getCoordLon(), coords.getCoordLat()));
            }
        } else {
            line.getCoordinates().add(new LngLatAlt(flight.getDeparturePosition().getCoordLon(), flight.getDeparturePosition().getCoordLat()));
            line.getCoordinates().add(new LngLatAlt(flight.getArrivalPosition().getCoordLon(), flight.getArrivalPosition().getCoordLat()));
        }

        feature.setProperty("title", flight.getSimAircraft().getDescription() + " ( " + flight.getFormatedDistance() + ")");

        feature.setGeometry(line);
        featureCollection.add(feature);
    }


}
