package ch.nostromo.flightsimlog.data;

import ch.nostromo.flightsimlog.data.base.*;
import ch.nostromo.flightsimlog.data.flight.FlightSim;
import ch.nostromo.flightsimlog.data.flight.Flight;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Getter
@Setter
@Data
@XmlRootElement(name = "logbook")
@XmlAccessorType(XmlAccessType.FIELD)
public class Logbook {

    @XmlElement
    public Integer flightIdSequence = 0;

    @XmlElement
    public Integer simAircraftSequence = 0;

    @XmlElement
    public Integer aircraftSequence = 0;

    @XmlElement
    public Integer categoryIdSequence = 0;

    @XmlElement
    public FlightSim defaultFlightsim = FlightSim.MSFS_2020;

    @XmlElementWrapper(name = "categories")
    @XmlElement(name = "category")
    public List<Category> categories = new ArrayList<>();


    @XmlElementWrapper(name = "aircraft")
    @XmlElement(name = "aircraft")
    public List<Aircraft> aircraft = new ArrayList<>();


    @XmlElementWrapper(name = "simAircraft")
    @XmlElement(name = "simAircraft")
    public List<SimAircraft> simAircraft = new ArrayList<>();

    @XmlElementWrapper(name = "flights")
    @XmlElement(name = "flight")
    public List<Flight> flights = new ArrayList<>();

    @XmlTransient
    public File logbookFile;

    public Flight getFlightById(String id) {
        for (Flight flight : flights) {
            if (flight.getId().equals(id)) {
                return flight;
            }
        }
        throw new IllegalArgumentException("Unknown ID: " + id);
    }

    public List<SimAircraft> getSortedSimAircraft() {
        List<SimAircraft> result = new ArrayList<>(simAircraft);

        result.sort(Comparator.comparing(SimAircraft::getDescription));

        return result;
    }

    public List<Aircraft> getFilteredAircraftList(String filter, AircraftType currentFilterAircraftType, AircraftSeatingType currentFilterSeatingType, boolean showOutdated) {
        List<Aircraft> result = new ArrayList<>();
        for (Aircraft aircraft : aircraft) {
            if (filter == null || filter.isEmpty() || aircraft.getDescription().toUpperCase().contains(filter.toUpperCase()) || aircraft.getManufacturer().toUpperCase().contains(filter.toUpperCase()) || aircraft.getTags().toUpperCase().contains(filter.toUpperCase()) ) {
                if (currentFilterAircraftType == null ||aircraft.getAircraftType().equals(currentFilterAircraftType)) {
                    if (currentFilterSeatingType == null || aircraft.getAircraftSeatingType().equals(currentFilterSeatingType)) {
                        if (!aircraft.getOutdated() || showOutdated) {
                            result.add(aircraft);
                        }

                    }
                }

            }
        }



        result.sort(Comparator.comparing(Aircraft::getDescription));

        return result;
    }

    public List<Flight> getFilteredFlightList(Category category) {
        List<Flight> result = new ArrayList<>();

        for (Flight flight : flights) {
            if (category == null || flight.getCategory().equals(category)) {
                result.add(flight);
            }
        }

        result.sort(Comparator.comparing(Flight::getComputerDepartureTime).reversed());

        return result;
    }

    public int getFlightCountByAircraft(Aircraft aircraft) {
        int count = 0;
        for (Flight flight : getFlights()) {
            if (aircraft.getSimAircraft().contains(flight.getSimAircraft())) {
                count++;
            }

        }
        return count;
    }

    public List<Flight> getFlightsByAircraft(Aircraft aircraft) {
        List<Flight> result = new ArrayList<>();

        for (Flight flight : getFlights()) {
            if (aircraft.getSimAircraft().contains(flight.getSimAircraft())) {
               result.add(flight);
            }

        }
        return result;
    }

    public boolean isSimAircraftUnlinked(SimAircraft simAircraft) {
        for (Aircraft aircraft : aircraft) {
            if (aircraft.getSimAircraft().contains(simAircraft)) {
                return false;
            }
        }
        return true;
    }

    public List<SimAircraft> getUnlinkedSimAircraft() {
        List<SimAircraft> result = new ArrayList<>();
        for (SimAircraft simAircraft : simAircraft) {
            if (isSimAircraftUnlinked(simAircraft)) {
                result.add(simAircraft);
            }
        }

        return result;

    }

    public String getNextFlightId() {
        flightIdSequence++;
        return String.valueOf(flightIdSequence);
    }

    public String getNextSimAircraftId() {
        simAircraftSequence++;
        return "S-" + simAircraftSequence;
    }

    public String getNextCategoryId() {
        categoryIdSequence++;
        return "C-" + categoryIdSequence;
    }

    public String getNextAircraftId() {
        aircraftSequence++;
        return "A-" + aircraftSequence;
    }

    public boolean isCategoryInUse(Category selectedItem) {
        for (Flight flight : flights) {
            if (flight.getCategory().equals(selectedItem)) {
                return true;
            }
        }

        return false;
    }
}
