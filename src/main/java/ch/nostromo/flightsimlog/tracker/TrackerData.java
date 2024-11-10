package ch.nostromo.flightsimlog.tracker;

import ch.nostromo.flightsimlog.data.coordinates.SimulationMeasurement;
import lombok.Data;

import java.util.Calendar;

@Data
public class TrackerData {

    SimulationMeasurement simulationMeasurement;

    Calendar localTime;

    String aircraft;

    String gpsPrev;

    String gpsNext;

    @Override
    public String toString() {
        return "TrackerData{" +
                "simulationMeasurements=" + simulationMeasurement +
                ", localTime=" + localTime +
                ", aircraft='" + aircraft + '\'' +
                ", gpsPrev='" + gpsPrev + '\'' +
                ", gpsNext='" + gpsNext + '\'' +
                '}';
    }
}
