package ch.nostromo.flightsimlog.reports;

import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Aircraft;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Reports {

    private static final String TEMPLATE_LIST = "template_list.html";
    private static final String TEMPLATE_MAP = "template_map.html";
    private static final String TEMPLATE_FLIGHT = "template_flight.html";



    public static void createReports(Logbook logbook) {

        try {
            // List of all categories
            createCategoriesPage(logbook);
            createAircraftPage(logbook);

        } catch (IOException e) {
            throw new FlightSimLogException("Unable to create statistics with error: " + e.getMessage(), e);
        }


    }


    private static String mainNavigation() {
       return "<h3>Logbook - <a href=\"index.html\">Categories</a> / <a href=\"aircraft.html\">Aircraft</a> </h3>";

    }

    public static void createCategoriesPage(Logbook logbook) throws IOException {

        Breadcrumbs breadcrumbs = new Breadcrumbs();
        Breadcrumb currentPage = new Breadcrumb("Categories", "index.html", "");

        breadcrumbs.breadcrumbs.add(currentPage);

        String template = loadTemplate(TEMPLATE_MAP);

        template = template.replace("%%TITLE%%", mainNavigation());
        template = template.replace("%%NAVIGATION%%", breadcrumbs.getNavigation());

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

            if (category.getGenerateReport()) {

                List<Flight> categoryFlights = logbook.getFilteredFlightList(category);

                if (!categoryFlights.isEmpty()) {

                    HtmlTableRow row = new HtmlTableRow();

                    Breadcrumbs subBreadcrumbs = breadcrumbs.clone();
                    Breadcrumb flightsBreadCrumb = new Breadcrumb(category.getDescription(), "flights-category-" + category.getId() + ".html", category.getId());
                    subBreadcrumbs.getBreadcrumbs().add(flightsBreadCrumb);

                    row.addField(category.getDescription(), colTitle, flightsBreadCrumb.getLink());
                    row.addField(categoryFlights.size(), colFlights);
                    table.getRows().add(row);

                    createListOfFlights(logbook, subBreadcrumbs, categoryFlights, category.getGenerateDetailedRoute());
                }
            }
        }


        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(logbook.getFlightsForReport(), false, false);
        template = template.replace("%%GEOJSON%%", geoJson);


        writeFile(logbook, currentPage.getLink(), template);

    }

    public static void createAircraftPage(Logbook logbook) throws IOException {

        Breadcrumbs breadcrumbs = new Breadcrumbs();
        Breadcrumb currentPage = new Breadcrumb("Aircraft", "aircraft.html", "");

        breadcrumbs.breadcrumbs.add(currentPage);

        String template = loadTemplate(TEMPLATE_MAP);

        template = template.replace("%%TITLE%%", mainNavigation());
        template = template.replace("%%NAVIGATION%%", breadcrumbs.getNavigation());

        HtmlTable table = new HtmlTable();

        HtmlTableColumn colAircraft = new HtmlTableColumn("Aircraft", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colType = new HtmlTableColumn("Aircraft Type", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colFlights = new HtmlTableColumn("Flights", HtmlTableColumn.HtmlColumnType.STRING);


        List<Aircraft> sortedAircrafts = logbook.getAircraft();

        sortedAircrafts.sort((o1, o2) -> {
            int o1Count = logbook.getFlightCountByAircraftForReport(o1);
            int o2Count = logbook.getFlightCountByAircraftForReport(o2);

            return Integer.compare(o2Count, o1Count);
        });


        for (Aircraft aircraft : sortedAircrafts) {

            List<Flight> categoryFlights = logbook.getFlightsByAircraftForReport(aircraft);
            HtmlTableRow row = new HtmlTableRow();

            Breadcrumbs subBreadcrumbs = breadcrumbs.clone();
            Breadcrumb flightsBreadCrumb = new Breadcrumb(aircraft.getManufacturer() + " - " + aircraft.getDescription(), "flights-aircraft-" + aircraft.getId() + ".html", aircraft.getId());
            subBreadcrumbs.getBreadcrumbs().add(flightsBreadCrumb);


            row.addField(aircraft.getManufacturer() + " - " + aircraft.getDescription(), colAircraft, flightsBreadCrumb.getLink());
            row.addField(aircraft.getAircraftType(), colType); // Possible type list

            row.addField(categoryFlights.size(), colFlights);
            table.getRows().add(row);

            createListOfFlights(logbook, subBreadcrumbs, categoryFlights, true);
        }


        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(logbook.getFlightsForReport(), false, false);
        template = template.replace("%%GEOJSON%%", geoJson);


        writeFile(logbook, currentPage.getLink(), template);

    }

    public static void createListOfFlights(Logbook logbook, Breadcrumbs breadcrumbs, List<Flight> flights, boolean detailedFlightPath) throws IOException {

        String template = loadTemplate(TEMPLATE_MAP);
        String pageLink = breadcrumbs.getBreadcrumbs().getLast().getLink();

        template = template.replace("%%TITLE%%", mainNavigation());
        template = template.replace("%%NAVIGATION%%", breadcrumbs.getNavigation());

        HtmlTable table = new HtmlTable();

        HtmlTableColumn colDate = new HtmlTableColumn("Date", HtmlTableColumn.HtmlColumnType.CALENDAR);
        HtmlTableColumn colCategory = new HtmlTableColumn("Category", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colDescription = new HtmlTableColumn("Description", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colDistance = new HtmlTableColumn("Distance", HtmlTableColumn.HtmlColumnType.DISTANCE);
        HtmlTableColumn colDuration = new HtmlTableColumn("Duration", HtmlTableColumn.HtmlColumnType.TIME);
        HtmlTableColumn colAircraft = new HtmlTableColumn("Aircraft", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colFrom = new HtmlTableColumn("From", HtmlTableColumn.HtmlColumnType.STRING);
        HtmlTableColumn colTo = new HtmlTableColumn("To", HtmlTableColumn.HtmlColumnType.STRING);

        for (Flight flight : flights) {

            Breadcrumbs subBreadcrumbs = breadcrumbs.clone();
            Breadcrumb flightsBreadCrumb = new Breadcrumb(flight.getDescription(), "flight-" + breadcrumbs.breadcrumbs.getLast().getId() + "-" + flight.getId() + ".html", flight.getId());
            subBreadcrumbs.getBreadcrumbs().add(flightsBreadCrumb);


            HtmlTableRow row = new HtmlTableRow();
            row.addField(flight.getComputerDepartureTime(), colDate);
            row.addField(flight.getDescription(), colDescription, flightsBreadCrumb.link);
            row.addField(flight.getDeparturePosition().getIcao(), colFrom);
            row.addField(flight.getArrivalPosition().getIcao(), colTo);
            row.addField(flight.getCalculatedDistanceInNm(), colDistance);
            row.addField(flight.getCalculatedDurationInSec(), colDuration);
            row.addField(logbook.getAircraftBySimAircraft(flight.getSimAircraft()).getManufacturer() + " - " + logbook.getAircraftBySimAircraft(flight.getSimAircraft()).getDescription(), colAircraft);
            row.addField(flight.getCategory().getDescription(), colCategory);
            table.getRows().add(row);

            createFlight(logbook, subBreadcrumbs, flight);
        }

        template = template.replace("%%CONTENT%%", table.getHtmlTable());

        String geoJson = GeoJson.createGeoJson(flights, true, detailedFlightPath);
        template = template.replace("%%GEOJSON%%", geoJson);

        writeFile(logbook, pageLink, template);

    }


    public static void createFlight(Logbook logbook, Breadcrumbs breadcrumbs, Flight flight) throws IOException {

        String template = loadTemplate(TEMPLATE_FLIGHT);
        String pageLink = breadcrumbs.getBreadcrumbs().getLast().getLink();

        template = template.replace("%%TITLE%%", mainNavigation());
        template = template.replace("%%NAVIGATION%%", breadcrumbs.getNavigation());

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
        String geoJson = GeoJson.createGeoJson(flights, true, true);
        template = template.replace("%%GEOJSON%%", geoJson);

        writeFile(logbook, pageLink, template);


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
