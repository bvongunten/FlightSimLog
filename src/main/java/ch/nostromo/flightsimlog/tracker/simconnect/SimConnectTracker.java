package ch.nostromo.flightsimlog.tracker.simconnect;

import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.coordinates.SimulationMeasurement;
import ch.nostromo.flightsimlog.tracker.TrackerData;
import ch.nostromo.flightsimlog.tracker.TrackerListener;
import lombok.Data;
import org.lembeck.fs.simconnect.SimConnect;
import org.lembeck.fs.simconnect.SimUtil;
import org.lembeck.fs.simconnect.constants.DataType;
import org.lembeck.fs.simconnect.constants.SimconnectPeriod;
import org.lembeck.fs.simconnect.response.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;


@Data
public class SimConnectTracker {


    TrackerListener listener = null;
    SimConnect simConnect;
    String host;
    int port;

    public SimConnectTracker(String host, int port, TrackerListener listener) {
        this.host = host;
        this.port = port;

        this.listener = listener;

    }


    public void stopTracker() {
        if (simConnect != null) {
            try {
                simConnect.close();
            } catch (Exception ignored) {
                // Nothing
            }
        }

        listener.onDisconnected();
    }

    public void startTracker() {


        try {
            simConnect = new SimConnect();

            simConnect.connect(host, port, "FlightSimLogClient");

            simConnect.subscribeToSystemEvent(1, "Sim");
            simConnect.subscribeToSystemEvent(2, "4sec");
            simConnect.subscribeToSystemEvent(3, "SimStart");
            simConnect.subscribeToSystemEvent(4, "SimStop");
            simConnect.subscribeToSystemEvent(5, "Pause_EX1");
            simConnect.subscribeToSystemEvent(6, "FlightLoaded");


            simConnect.getRequestReceiver().addExceptionHandler(this::handleException);
            simConnect.getRequestReceiver().addEventHandler(this::handleEvent);
            simConnect.getRequestReceiver().addSimobjectDataHandler(this::handleSimObject);
            simConnect.getRequestReceiver().addEventFilenameHandler(this::handleEventFilename);


            simConnect.addToDataDefinition(1, "PLANE LATITUDE", "DEGREES", DataType.FLOAT64, 0);
            simConnect.addToDataDefinition(1, "PLANE LONGITUDE", "DEGREES", DataType.FLOAT64, 0);
            simConnect.addToDataDefinition(1, "PLANE ALTITUDE", "FEET", DataType.FLOAT64, 0);

            simConnect.addToDataDefinition(1, "TITLE", null, DataType.STRING128, 0);
            simConnect.addToDataDefinition(1, "GPS WP NEXT ID", null, DataType.STRING128, 0);
            simConnect.addToDataDefinition(1, "GPS WP PREV ID", null, DataType.STRING128, 0);

            simConnect.addToDataDefinition(1, "LOCAL DAY OF YEAR", "NUMBER", DataType.INT32, 0);
            simConnect.addToDataDefinition(1, "LOCAL TIME", "SECONDS", DataType.INT32, 0);
            simConnect.addToDataDefinition(1, "LOCAL YEAR", "NUMBER", DataType.INT32, 0);
            simConnect.addToDataDefinition(1, "SIM ON GROUND", "BOOL", DataType.INT32, 0);

            simConnect.requestDataOnSimObject(1, 1, 0, SimconnectPeriod.SECOND, 0, 0, 0, 0);

            listener.onConnected();


        } catch (IOException e) {
            listener.onException(e);
        }


    }


    public void handleSimObject(RecvSimobjectDataResponse response) {
        ByteBuffer data = response.getData();


        double userLat = data.getDouble();
        double userLon = data.getDouble();
        double userAlt = data.getDouble();


        String aircraft = SimUtil.readString(data, 128);
        String gpsNextId = SimUtil.readString(data, 128);
        String gpsPrevId = SimUtil.readString(data, 128);

        int dayOfYear = data.getInt();
        int time = data.getInt();
        int year = data.getInt();

        int simOnGround = data.getInt();


        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
        cal.set(Calendar.SECOND, time);
        cal.set(Calendar.YEAR, year);

        TrackerData trackerData = new TrackerData();

        SimulationMeasurement measurement = new SimulationMeasurement();
        measurement.setTime(cal.getTime().getTime());
        measurement.setOnGround(simOnGround == 1);
        measurement.setCoordLon(userLon);
        measurement.setCoordLat(userLat);
        measurement.setCoordAlt(userAlt);


        trackerData.setSimulationMeasurement(measurement);
        trackerData.setAircraft(aircraft);
        trackerData.setGpsNext(gpsNextId);
        trackerData.setGpsPrev(gpsPrevId);

        listener.onData(trackerData);

    }


    private void handleEventFilename(RecvEventFilenameResponse recvEventFilenameResponse) {
        listener.onEventFileLoaded(recvEventFilenameResponse.getFilename());

    }

    public void handleEvent(RecvEventResponse recvEventResponse) {
        switch (recvEventResponse.getEventID()) {
            case 1:
                listener.onEventPause(recvEventResponse.getEventID());
                break;
            case 5:
                listener.onEventSim(recvEventResponse.getEventID());
                break;
        }
    }


    public void handleException(RecvExceptionResponse e) {
        listener.onException(new FlightSimLogException("Recv exception response: " + e.getExceptionType()));
        stopTracker();
    }


}
