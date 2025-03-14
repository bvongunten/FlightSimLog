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

import static java.lang.Math.*;

public class GeoJson {

    public static String createGeoJson(Flight flight, boolean includePath, boolean detailPath) {
        return createGeoJson(new ArrayList<>(List.of(flight)), includePath, detailPath);
    }

    public static String createGeoJson(List<Flight> flights, boolean includePath, boolean detailPath) {
        FeatureCollection featureCollection = new FeatureCollection();

        Set<String> visitedIcaos = new HashSet<>();

        for (Flight flight : flights) {
            addPosition(featureCollection, visitedIcaos, flight.getDeparturePosition());
            addPosition(featureCollection, visitedIcaos, flight.getArrivalPosition());

            if (includePath) {
                addPath(featureCollection, flight, detailPath);
            }

        }


        try {
            return new ObjectMapper().writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            throw new FlightSimLogException("unable to create json", e);
        }
    }

    public static String createGeoJson(List<Coordinates> coordinates) {
        FeatureCollection featureCollection = new FeatureCollection();

        addPath(featureCollection, coordinates);

        try {
            return new ObjectMapper().writeValueAsString(featureCollection);
        } catch (JsonProcessingException e) {
            throw new FlightSimLogException("unable to create json", e);
        }

    }


    private static void addPosition(FeatureCollection featureCollection, Set<String> visitedIcaos, WorldPosition position) {

        String icao = position.getIcao();
        if (icao == null) {
            icao = "";
        }

        String description = position.getDescription();

        if (!icao.isEmpty() && visitedIcaos.contains(icao)) {
            return;
        }

        visitedIcaos.add(icao);
        Feature feature = new Feature();

        Point point = new Point();
        point.setCoordinates(new LngLatAlt(position.getCoordLon(), position.getCoordLat()));
        feature.setGeometry(point);

        if (icao.isEmpty()) {
            feature.setProperty("name", description);
        } else {
            feature.setProperty("name", icao + " - " + description);
        }

        featureCollection.add(feature);

    }

    private static void addPath(FeatureCollection featureCollection, Flight flight, boolean detailPath) {
        List<Coordinates> coordinates = new ArrayList<>();

        if (flight.getSimulationData().getMeasurements().size() > 1 && detailPath) {
            coordinates.addAll(flight.getSimulationData().getMeasurements());
        } else {
            coordinates.add(flight.getDeparturePosition());
            coordinates.add(flight.getArrivalPosition());
        }

        addPath(featureCollection, coordinates);

    }

    public static void addPath(FeatureCollection featureCollection, List<Coordinates> coordinates) {

        Coordinates lastCoordinates = null;

        Feature mainLineFeature = new Feature();
        LineString lineString = new LineString();

        for (Coordinates coordinate : coordinates) {

            if (lastCoordinates != null) {

                double lonDifference = Math.abs(coordinate.getCoordLon() - lastCoordinates.getCoordLon());

                if (lonDifference > 180) {

                    if (lastCoordinates.getCoordLon() > 0 && coordinate.getCoordLon() < 0) {
                        // Crossing from + to -
                        Feature firstLineFeature = new Feature();
                        LineString firstLine = new LineString();

                        firstLine.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                        firstLine.getCoordinates().add(new LngLatAlt(179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), 179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));

                        firstLineFeature.setGeometry(firstLine);
                        featureCollection.add(firstLineFeature);

                        Feature secondLineFeature = new Feature();
                        LineString secondLine = new LineString();

                        secondLine.getCoordinates().add(new LngLatAlt(-179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), -179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        secondLine.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));

                        secondLineFeature.setGeometry(secondLine);
                        featureCollection.add(secondLineFeature);

                    } else if (lastCoordinates.getCoordLon() < 0 && coordinate.getCoordLon() > 0) {
                        // Crossing from - to +
                        Feature firstLineFeature = new Feature();
                        LineString firstLine = new LineString();

                        firstLine.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                        firstLine.getCoordinates().add(new LngLatAlt(-179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), -179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));

                        firstLineFeature.setGeometry(firstLine);
                        featureCollection.add(firstLineFeature);

                        Feature secondLineFeature = new Feature();
                        LineString secondLine = new LineString();

                        secondLine.getCoordinates().add(new LngLatAlt(179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), 179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        secondLine.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));

                        secondLineFeature.setGeometry(secondLine);
                        featureCollection.add(secondLineFeature);
                    }

                } else {

                    lineString.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                    lineString.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));

                }
            }

            lastCoordinates = coordinate;

        }

        mainLineFeature.setGeometry(lineString);
        featureCollection.add(mainLineFeature);
    }


    /**
     * Vincenty's formulae to calculate the latitude at an intermediate longitude.
     */
    private static double vincentyLat(double lat1, double lon1, double lonIntermediate, double lat2, double lon2) {
        // Convert degrees to radians
        lat1 = toRadians(lat1);
        lon1 = toRadians(lon1);
        lat2 = toRadians(lat2);
        lon2 = toRadians(lon2);
        lonIntermediate = toRadians(lonIntermediate);

        // Vincenty's constants
        double a = 6378137.0; // Equatorial radius in meters
        double f = 1 / 298.257223563; // Flattening
        double b = (1 - f) * a;

        double dLon = lon2 - lon1;

        double U1 = atan((1 - f) * tan(lat1));
        double U2 = atan((1 - f) * tan(lat2));
        double sinU1 = sin(U1), cosU1 = cos(U1);
        double sinU2 = sin(U2), cosU2 = cos(U2);

        double sinSigma, cosSigma, sigma, sinAlpha, cos2Alpha, cos2SigmaM;
        double lambda = dLon, lambdaP;
        int iterLimit = 100;
        do {
            double sinLambda = sin(lambda), cosLambda = cos(lambda);
            sinSigma = sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cos2Alpha;
            lambdaP = lambda;
            lambda = dLon + (1 - cos2Alpha) * f * sinAlpha * (sigma + f * sinSigma * (cos2SigmaM + f * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        } while (abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        return toDegrees(atan2(sinU1 + sinU2, sqrt((cosU1 + cosU2) * (cosU1 + cosU2) + cos2SigmaM * cos2SigmaM)));
    }

}
