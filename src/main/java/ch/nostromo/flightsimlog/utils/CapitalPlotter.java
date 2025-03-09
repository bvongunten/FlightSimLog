package ch.nostromo.flightsimlog.utils;

import ch.nostromo.flightsimlog.data.coordinates.Coordinates;
import ch.nostromo.flightsimlog.utils.CsvContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;


public class CapitalPlotter {



    public static void main(String... args) throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("capitals.csv").toURI());

        CsvContent csvContent = new CsvContent(path, 6);

        FeatureCollection featureCollection = new FeatureCollection();

        List<Coordinates> coordinates = new ArrayList<>();

        for (int i = 0; i < csvContent.getValues().size(); i++) {
            List<String> currentEntry = csvContent.getValues().get(i);
            Coordinates coordinate = new Coordinates(Double.parseDouble(currentEntry.get(2)), Double.parseDouble(currentEntry.get(3)), 0.0);
            coordinates.add(coordinate);

            Feature feature = new Feature();
            Point point = new Point();
            point.setCoordinates(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));
            feature.setProperty("name", currentEntry.get(0) + " - " + currentEntry.get(1));
            feature.setGeometry(point);
            featureCollection.add(feature);

        }

        Coordinates lastCoordinates = null;

        for (Coordinates coordinate : coordinates) {

            if (lastCoordinates != null) {
                Feature lineFeature = new Feature();
                LineString line = new LineString();

                double lonDifference = Math.abs(coordinate.getCoordLon() - lastCoordinates.getCoordLon());

                if (lonDifference > 180) {

                    if (lastCoordinates.getCoordLon() > 0 && coordinate.getCoordLon() < 0) {
                        // Crossing from + to -
                        LineString firstLine = new LineString();
                        firstLine.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                        firstLine.getCoordinates().add(new LngLatAlt(179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), 179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        lineFeature.setGeometry(firstLine);
                        featureCollection.add(lineFeature);

                        Feature secondLineFeature = new Feature();
                        LineString secondLine = new LineString();
                        secondLine.getCoordinates().add(new LngLatAlt(-179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), -179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        secondLine.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));
                        secondLineFeature.setGeometry(secondLine);
                        featureCollection.add(secondLineFeature);

                    } else if (lastCoordinates.getCoordLon() < 0 && coordinate.getCoordLon() > 0) {
                        // Crossing from - to +
                        LineString firstLine = new LineString();
                        firstLine.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                        firstLine.getCoordinates().add(new LngLatAlt(-179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), -179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        lineFeature.setGeometry(firstLine);
                        featureCollection.add(lineFeature);

                        Feature secondLineFeature = new Feature();
                        LineString secondLine = new LineString();
                        secondLine.getCoordinates().add(new LngLatAlt(179.9, vincentyLat(lastCoordinates.getCoordLat(), lastCoordinates.getCoordLon(), 179.9, coordinate.getCoordLat(), coordinate.getCoordLon())));
                        secondLine.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));
                        secondLineFeature.setGeometry(secondLine);
                        featureCollection.add(secondLineFeature);
                    }

                } else {
                    // If no crossing, just create one line
                    line.getCoordinates().add(new LngLatAlt(lastCoordinates.getCoordLon(), lastCoordinates.getCoordLat()));
                    line.getCoordinates().add(new LngLatAlt(coordinate.getCoordLon(), coordinate.getCoordLat()));
                    lineFeature.setGeometry(line);
                    featureCollection.add(lineFeature);
                }
            }




            // Update the last known coordinates and label for the next iteration
            lastCoordinates = coordinate;
        }

        System.out.println(new ObjectMapper().writeValueAsString(featureCollection));
    }

    /**
     * Vincenty's formulae to calculate the latitude at an intermediate longitude.
     *
     * @param lat1            Latitude of the starting point
     * @param lon1            Longitude of the starting point
     * @param lonIntermediate The intermediate longitude to calculate latitude for
     * @param lat2            Latitude of the end point
     * @param lon2            Longitude of the end point
     * @return Interpolated latitude at the intermediate longitude
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
