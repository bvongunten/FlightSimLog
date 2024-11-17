package ch.nostromo.flightsimlog.airports;

import ch.nostromo.flightsimlog.data.coordinates.Coordinates;
import ch.nostromo.flightsimlog.utils.CsvContent;
import ch.nostromo.flightsimlog.utils.GeoTools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AirportsService {

    private static final String AIRPORTS_FILE = "ourairports.csv";

    static AirportsService instance;

    List<Airport> airports;

    public AirportsService()  {
        this.airports = loadAirports();
    }

    public static AirportsService getInstance() {
        if (instance == null) {
            instance = new AirportsService();
        }

        return instance;
    }

    private List<Airport> loadAirports() {

        try {
            List<Airport> result = new ArrayList<>();

            Path path = Paths.get(ClassLoader.getSystemResource(AIRPORTS_FILE).toURI());
            CsvContent csvContent = new CsvContent(path, 18);

            for (List<String> line : csvContent.getValues()) {

                Airport airport = new Airport();
                airport.setIcao(line.get(1));
                airport.setName(line.get(3));

                double lat = Double.parseDouble(line.get(4));
                double lon = Double.parseDouble(line.get(5));

                double alt = 0;
                if (!line.get(6).isEmpty()) {
                   alt = Double.parseDouble(line.get(6));
                }

                airport.setCoordinates(new Coordinates(lat, lon, alt));


                result.add(airport);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Airport getAirportByIcao(String icao) {
        for (Airport airport : airports) {
            if (airport.getIcao().equalsIgnoreCase(icao)) {
                return airport;
            }
        }

        return null;
    }

    public Airport getClosestAirport(double latitude, double longitude) {
        double shortestDistance = Double.MAX_VALUE;
        Airport result = null;

        for (Airport airport : airports) {
            double distance = GeoTools.calculateDistance(latitude, longitude, airport.getCoordinates().getCoordLat(), airport.getCoordinates().getCoordLon(), "N" );

            if (distance < shortestDistance) {
                result = airport;
                shortestDistance = distance;
            }

        }
        return result;
    }

}
