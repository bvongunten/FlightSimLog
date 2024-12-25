package ch.nostromo.flightsimlog.reports;

import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.fxui.dialogs.TextMessageDialog;
import ch.nostromo.flightsimlog.reports.html.HtmlTable;
import ch.nostromo.flightsimlog.reports.html.HtmlTableColumn;
import ch.nostromo.flightsimlog.reports.html.HtmlTableRow;
import ch.nostromo.flightsimlog.utils.GeoJson;
import ch.nostromo.flightsimlog.utils.OutputFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Reports {

    private static final String TEMPLATE_LIST = "template_list.html";
    private static final String TEMPLATE_MAP = "template_map.html";
    private static final String TEMPLATE_FLIGHT = "template_flight.html";

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


    public static void createReports(Logbook logbook) {

        try {
            // List of all categories
            createMainPage(logbook);
            createStatsPage(logbook);

        } catch (IOException e) {
            throw new FlightSimLogException("Unable to create statistics with error: " + e.getMessage(), e);
        }


    }


    public static void createStatsPage(Logbook logbook) throws IOException {

        String template = loadTemplate(TEMPLATE_MAP);

        template = template.replace("%%TITLE%%", "<h3><a href=\"stats.html\">Logbook</a> / Stats </h3>");
        template = template.replace("%%SUBTITLE%%", "<h4>Aircrafts</h4>");

        HtmlTable table = new HtmlTable();

        HtmlTableColumn colTitle = new HtmlTableColumn("Category", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colFlights = new HtmlTableColumn("Flights", HtmlTableColumn.HtmlColumnType.STRING);


        List<Category> sortedCategories = logbook.getCategories();

        sortedCategories.sort((o1, o2) -> {
            int o1Count = logbook.getFilteredFlightList(o1).size();
            int o2Count = logbook.getFilteredFlightList(o2).size();

            return Integer.compare(o2Count, o1Count);
        });


        for (Category category : sortedCategories) {

            List<Flight> categoryFlights = logbook.getFilteredFlightList(category);

            if (!categoryFlights.isEmpty()) {

                HtmlTableRow row = new HtmlTableRow();

                row.addField(category.getDescription(), colTitle, "category-" + category.getId() + ".html");
                row.addField(categoryFlights.size(), colFlights);
                table.getRows().add(row);

                createCategory(logbook, category, categoryFlights);
            }
        }


        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(logbook.getFlights(), false);
        template = template.replace("%%GEOJSON%%", geoJson);


        writeFile(logbook, "index.html", template);

    }


    public static void createMainPage(Logbook logbook) throws IOException {

        String template = loadTemplate(TEMPLATE_MAP);

        template = template.replace("%%TITLE%%", "<h3>Logbook / <a href=\"stats.html\">Statistics</a></h3>");
        template = template.replace("%%SUBTITLE%%", "<h4>Categories</h4>");

        HtmlTable table = new HtmlTable();

        HtmlTableColumn colTitle = new HtmlTableColumn("Category", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colFlights = new HtmlTableColumn("Flights", HtmlTableColumn.HtmlColumnType.STRING);


        List<Category> sortedCategories = logbook.getCategories();

        sortedCategories.sort((o1, o2) -> {
            int o1Count = logbook.getFilteredFlightList(o1).size();
            int o2Count = logbook.getFilteredFlightList(o2).size();

            return Integer.compare(o2Count, o1Count);
        });


        for (Category category : sortedCategories) {

            List<Flight> categoryFlights = logbook.getFilteredFlightList(category);

            if (!categoryFlights.isEmpty()) {

                HtmlTableRow row = new HtmlTableRow();

                row.addField(category.getDescription(), colTitle, "category-" + category.getId() + ".html");
                row.addField(categoryFlights.size(), colFlights);
                table.getRows().add(row);

                createCategory(logbook, category, categoryFlights);
            }
        }


        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(logbook.getFlights(), false);
        template = template.replace("%%GEOJSON%%", geoJson);


        writeFile(logbook, "index.html", template);

    }

    public static void createCategory(Logbook logbook, Category category, List<Flight> flights) throws IOException {

        String template = loadTemplate(TEMPLATE_MAP);


        template = template.replace("%%TITLE%%", "<h3><a href=\"index.html\">Logbook</a> - " + category.getDescription() + "</h3>");

        template = template.replace("%%SUBTITLE%%", "<h4>Flights</h4>");

        HtmlTable table = new HtmlTable();

        HtmlTableColumn colDate = new HtmlTableColumn("Date", HtmlTableColumn.HtmlColumnType.CALENDAR);
        HtmlTableColumn colDescription = new HtmlTableColumn("Description", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colDistance = new HtmlTableColumn("Distance", HtmlTableColumn.HtmlColumnType.DISTANCE);
        HtmlTableColumn colDuration = new HtmlTableColumn("Duration", HtmlTableColumn.HtmlColumnType.TIME);
        HtmlTableColumn colAircraft = new HtmlTableColumn("Aircraft", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colFrom = new HtmlTableColumn("From", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colTo = new HtmlTableColumn("To", HtmlTableColumn.HtmlColumnType.STRING);

        for (Flight flight : flights) {
            HtmlTableRow row = new HtmlTableRow();
            row.addField(flight.getComputerDepartureTime(), colDate);
            row.addField(flight.getDescription(), colDescription, "flight-" + flight.getId() + ".html");
            row.addField(flight.getCalculatedDistanceInNm(), colDistance);
            row.addField(flight.getCalculatedDurationInSec(), colDuration);
            row.addField(flight.getSimAircraft().getDescription(), colAircraft);
            row.addField(flight.getDeparturePosition().getIcao(), colFrom);
            row.addField(flight.getArrivalPosition().getIcao(), colTo);
            table.getRows().add(row);

            createFlight(logbook, category, flight);
        }

        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(flights, true);
        template = template.replace("%%GEOJSON%%", geoJson);

        writeFile(logbook, "category-" + category.getId() + ".html", template);

    }


    public static void createFlight(Logbook logbook, Category category, Flight flight) throws IOException {

        String template = loadTemplate(TEMPLATE_FLIGHT);

        template = template.replace("%%TITLE%%", "<h3><a href=\"category-" + category.getId() + ".html\">" + category.getDescription() + "</a> - " + flight.getDescription() + "</h3>");

        template = template.replace("%%DESCRIPTION%%", flight.getDescription());
        template = template.replace("%%AIRCRAFT%%", flight.getSimAircraft().getDescription());


        template = template.replace("%%DISTANCE%%", OutputFormatter.createDistance(flight.getCalculatedDistanceInNm()));
        template = template.replace("%%DURATION%%", OutputFormatter.createDuration(flight.getCalculatedDurationInSec()));


        template = template.replace("%%DEPARTURE_TIME%%", OutputFormatter.createTime(flight.getDepartureTime()));

        if (flight.getDeparturePosition().getIcao() != null && !flight.getDeparturePosition().getIcao().isEmpty()) {
            template = template.replace("%%DEPARTURE_TEXT%%", flight.getDeparturePosition().getIcao() + " -" + flight.getDeparturePosition().getDescription());
        } else {
            template = template.replace("%%DEPARTURE_TEXT%%", flight.getDeparturePosition().getDescription());
        }
        template = template.replace("%%DEPARTURE_LON%%", String.valueOf(flight.getDeparturePosition().getCoordLon()));
        template = template.replace("%%DEPARTURE_LAT%%", String.valueOf(flight.getDeparturePosition().getCoordLat()));


        template = template.replace("%%ARRIVAL_TIME%%", OutputFormatter.createTime(flight.getArrivalTime()));

        if (flight.getArrivalPosition().getIcao() != null && !flight.getArrivalPosition().getIcao().isEmpty()) {
            template = template.replace("%%ARRIVAL_TEXT%%", flight.getArrivalPosition().getIcao() + " -" + flight.getArrivalPosition().getDescription());
        } else {
            template = template.replace("%%ARRIVAL_TEXT%%", flight.getArrivalPosition().getDescription());
        }
        template = template.replace("%%ARRIVAL_LON%%", String.valueOf(flight.getArrivalPosition().getCoordLon()));
        template = template.replace("%%ARRIVAL_LAT%%", String.valueOf(flight.getArrivalPosition().getCoordLat()));

        template = template.replace("%%DEPARTURE_PC%%", OutputFormatter.createTime(flight.getComputerDepartureTime()));
        template = template.replace("%%ARRIVAL_PC%%", OutputFormatter.createTime(flight.getComputerArrivalTime()));

        template = template.replace("%%ID%%", flight.getId());

        List<Flight> flights = new ArrayList<>();
        flights.add(flight);
        String geoJson = GeoJson.createGeoJson(flights, true);
        template = template.replace("%%GEOJSON%%", geoJson);

        writeFile(logbook, "flight-" + flight.getId() + ".html", template);


    }

    private static void writeFile(Logbook logbook, String fileName, String content) throws IOException {
        Path filePath = Path.of(logbook.getLogbookFile().getParentFile().getAbsolutePath() + "\\Reports\\" + fileName);
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }


    private static String loadTemplate(String template) {

        try (InputStream inputStream = Reports.class.getClassLoader().getResourceAsStream(template)) {
            assert inputStream != null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            throw new FlightSimLogException("Unable to load template with error: " + e.getMessage(), e);
        }

    }

}
