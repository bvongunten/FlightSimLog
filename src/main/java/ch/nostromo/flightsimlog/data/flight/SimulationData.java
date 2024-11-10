package ch.nostromo.flightsimlog.data.flight;

import ch.nostromo.flightsimlog.data.coordinates.SimulationMeasurement;
import ch.nostromo.flightsimlog.tracker.TrackerData;
import ch.nostromo.flightsimlog.utils.GeoTools;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@XmlRootElement(name = "simulationData")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationData {

    @XmlElement
    String gpsNext = "";

    @XmlElement
    String gpsPrev = "";

    @XmlElement
    String aircraft = "";

    @XmlElement
    List<SimulationMeasurement> measurements = new ArrayList<>();

    public void addTrackerData(TrackerData data) {

        if (getMeasurements().isEmpty()) {
            getMeasurements().add(data.getSimulationMeasurement());
        } else {
            SimulationMeasurement last = getMeasurements().get(getMeasurements().size() - 1);
            double distance = GeoTools.calculateDistance(last.getCoordLat(), last.getCoordLon(), data.getSimulationMeasurement().getCoordLat(), data.getSimulationMeasurement().getCoordLon(), "M");

            if (data.getSimulationMeasurement().isOnGround() && distance > 5.0) {
                getMeasurements().add(data.getSimulationMeasurement());
            } else if (!data.getSimulationMeasurement().isOnGround() && distance > 100.0) {
                getMeasurements().add(data.getSimulationMeasurement());
            }
        }

        aircraft = data.getAircraft();
        gpsNext = data.getGpsNext();
        gpsPrev = data.getGpsPrev();
    }

    public void merge(SimulationData simulationData) {


        getMeasurements().clear();
        for (SimulationMeasurement measurement : simulationData.getMeasurements()) {
           getMeasurements().add(measurement.createCopy());
        }

        aircraft = simulationData.getAircraft();
        gpsNext = simulationData.getGpsNext();
        gpsPrev = simulationData.getGpsPrev();


    }
}
