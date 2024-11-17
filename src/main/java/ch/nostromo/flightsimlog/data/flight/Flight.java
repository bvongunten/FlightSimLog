package ch.nostromo.flightsimlog.data.flight;

import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.base.FlightSim;
import ch.nostromo.flightsimlog.data.coordinates.WorldPosition;
import ch.nostromo.flightsimlog.utils.FileTools;
import ch.nostromo.flightsimlog.utils.GeoTools;
import ch.nostromo.flightsimlog.utils.LogBookTools;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NoArgsConstructor
public class Flight {

    @XmlElement
    String id = "";

    @XmlElement
    FlightSim flightSim = null;

    @XmlIDREF
    SimAircraft simAircraft = null;

    @XmlIDREF
    Category category = null;

    @XmlElement
    String description = "";

    @XmlElement
    WorldPosition departurePosition = new WorldPosition();

    @XmlElement
    Calendar departureTime;

    @XmlElement
    WorldPosition arrivalPosition = new WorldPosition();

    @XmlElement
    Calendar arrivalTime;

    @XmlElement
    Calendar computerDepartureTime;

    @XmlElement
    Calendar computerArrivalTime;

    @XmlTransient
    SimulationData simulationData = null;

    @XmlTransient
    File logbookFile;

    public Flight(Flight input) {
        this.merge(input);
    }

    public void merge(Flight input) {
        logbookFile = input.logbookFile;

        id = input.getId();
        flightSim = input.flightSim;
        simAircraft = input.simAircraft;
        category = input.category;
        description = input.description;
        departurePosition.setIcao(input.departurePosition.getIcao());
        departurePosition.setDescription(input.departurePosition.getDescription());
        departurePosition.setCoordLat(input.departurePosition.getCoordLat());
        departurePosition.setCoordLon(input.departurePosition.getCoordLon());
        departurePosition.setCoordAlt(input.departurePosition.getCoordAlt());
        if (input.departureTime != null) {
            departureTime = (Calendar) input.departureTime.clone();
        }


        arrivalPosition.setIcao(input.arrivalPosition.getIcao());
        arrivalPosition.setDescription(input.arrivalPosition.getDescription());
        arrivalPosition.setCoordLat(input.arrivalPosition.getCoordLat());
        arrivalPosition.setCoordLon(input.arrivalPosition.getCoordLon());
        arrivalPosition.setCoordAlt(input.arrivalPosition.getCoordAlt());

        if (input.arrivalTime != null) {
            arrivalTime = (Calendar) input.arrivalTime.clone();
        }

        if (input.computerDepartureTime != null) {
            computerDepartureTime = (Calendar) input.computerDepartureTime.clone();
        }


        if  (input.computerArrivalTime != null) {
            computerArrivalTime = (Calendar) input.computerArrivalTime.clone();
        }


        if (input.getSimulationData() != null) {
            simulationData = getSimulationData();
            simulationData.merge(input.getSimulationData());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flight flight = (Flight) o;

        return id != null ? id.equals(flight.id) : flight.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


    // **************************************** FXUI Table helpers *****************************************


    public double getDistanceNm() {
        if (departurePosition != null && arrivalPosition != null) {
            return GeoTools.calculateDistance(departurePosition, arrivalPosition, "N");
        }
        return 0.0d;
    }

    public int getAirSeconds() {
        if (arrivalTime != null && departureTime != null) {
            return (int) (arrivalTime.getTimeInMillis() - departureTime.getTimeInMillis()) / 1000;
        }

        return 0;
    }


    public String getFormatedFlightTime() {
        long seconds = getAirSeconds();

        // Calculate hours and minutes
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        // Format and return the string as "HH:MM"
        return String.format("%02d:%02d", hours, minutes);
    }

    public String getFormatedComputerDepartureTime() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(computerDepartureTime.getTime());
    }

    public String getFormatedDistance() {
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(getDistanceNm());
    }

    public String getFormatedDeparturePositionIcao() {
        return departurePosition.getIcao();
    }

    public String getFormatedArrivalPositionIcao() {
        return arrivalPosition.getIcao();
    }

    // ******************************** FILE HANDLING *******************************

    public File getFlightImagesPath() {
        return new File (getLogbookFile().getParentFile().getAbsolutePath() + "/Screenshots/" +  getId());
    }


    public int getImagesCount() {
        File dir = getFlightImagesPath();

        if (!dir.exists()) {
            return 0;
        }

        return FileTools.getPngsInDirectory(dir).length;
    }


    public SimulationData getSimulationData() {
        if (simulationData == null) {
            // Try to load from xml
            String fileName = getLogbookFile().getParentFile().getAbsolutePath() + "/simulationdata-" + this.id + ".xml";
            SimulationData externalSimulationDataXml = null;
            externalSimulationDataXml = LogBookTools.loadOrCreateSimulationData(fileName);

            this.simulationData = externalSimulationDataXml;
        }

        return this.simulationData;

    }

    public void saveSimulationData() {
        String fileName = getLogbookFile().getParentFile().getAbsolutePath() +  "/simulationdata-" + this.id + ".xml";

        LogBookTools.saveSimulationData(getSimulationData(), fileName);
    }


    public void deleteSimulationData() {
        String fileName = getLogbookFile().getParentFile().getAbsolutePath() +  "/simulationdata-" + this.id + ".xml";
        File fileToBeDeleted = new File(fileName);
        fileToBeDeleted.delete();
    }



}
