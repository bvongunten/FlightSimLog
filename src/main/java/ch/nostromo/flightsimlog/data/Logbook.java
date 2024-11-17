package ch.nostromo.flightsimlog.data;

import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.base.FlightSim;
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
    public Integer aircraftIdSequence = 0;

    @XmlElement
    public Integer categoryIdSequence = 0;

    @XmlElement
    public FlightSim defaultFlightsim = FlightSim.MSFS_2020;

    @XmlElementWrapper(name = "categories")
    @XmlElement(name = "category")
    public List<Category> categories = new ArrayList<>();

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

    public List<SimAircraft> getSortedAircraft() {
        List<SimAircraft> result = new ArrayList<>(simAircraft);

        result.sort(Comparator.comparing(SimAircraft::getDescription));

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


    public String getNextFlightId() {
        flightIdSequence++;
        return String.valueOf(flightIdSequence);
    }

    public String getNextAircraftId() {
        aircraftIdSequence++;
        return "A-" + String.valueOf(aircraftIdSequence);
    }

    public String getNextCategoryId() {
        categoryIdSequence++;
        return "C-" + String.valueOf(categoryIdSequence);
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
