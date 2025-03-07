package ch.nostromo.flightsimlog.tracker.autotracker;

import ch.nostromo.flightsimlog.FlightSimLogConfig;
import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.airports.Airport;
import ch.nostromo.flightsimlog.airports.AirportsService;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.coordinates.SimulationMeasurement;
import ch.nostromo.flightsimlog.data.coordinates.WorldPosition;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.data.flight.SimulationData;
import ch.nostromo.flightsimlog.tracker.TrackerData;
import ch.nostromo.flightsimlog.tracker.TrackerListener;
import ch.nostromo.flightsimlog.tracker.simconnect.SimConnectTracker;
import ch.nostromo.flightsimlog.utils.FlightScreenshotGrabber;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

public class AutoTracker implements TrackerListener {

    AutoTrackerListener listener;

    SimConnectTracker simConnectTracker;
    boolean inFlight = false;
    Flight currentFlight = null;

    String lastFile = "MainMenu";
    int lastSim;
    int lastPause;
    private int lastSimStart;
    private int lastSimStop;

    FlightScreenshotGrabber flightScreenshotGrabber;

    public AutoTracker(AutoTrackerListener listener) {
        this.listener = listener;
        simConnectTracker = new SimConnectTracker(FlightSimLogConfig.getSimConnectHost(), FlightSimLogConfig.getSimConnectPort(), this);
    }

    public void startTracker() {
        simConnectTracker.startTracker();
    }

    public void stopTracker() {
        simConnectTracker.stopTracker();

        if (flightScreenshotGrabber != null) {
            flightScreenshotGrabber.setRunning(false);
            flightScreenshotGrabber = null;
        }

      //  flightEnded();
    }

    private void descisionMaker() {


        // TBD ...


    }


    @Override
    public void onData(TrackerData data) {
        this.listener.onData(data);
        if (inFlight && currentFlight != null && data.getSimulationMeasurement().getCoordLat() != 0.00 && data.getSimulationMeasurement().getCoordLon() != 0.00) {
            currentFlight.getSimulationData().addTrackerData(data);
        }
    }


    @Override
    public void onEventFileLoaded(String file) {
        lastFile = file;
        this.listener.onEventFileLoaded(file);
        descisionMaker();
    }

    @Override
    public void onEventPause(int pause) {
        this.lastPause = pause;
        this.listener.onEventPause(pause);
        descisionMaker();
    }

    @Override
    public void onEventSimStart(int start) {
        this.lastSimStart = start;
        this.listener.onEventSimStart(start);
    }


    @Override
    public void onEventSimStop(int stop) {
        this.lastSimStop = stop;
        this.listener.onEventSimStop(stop);
    }

    @Override
    public void onEventSim(int sim) {
        this.lastSim = sim;
        this.listener.onEventSim(sim);
        descisionMaker();
    }

    @Override
    public void onConnected() {
        this.listener.onConnected();
    }

    @Override
    public void onDisconnected() {
        this.listener.onDisconnected();
    }

    @Override
    public void onException(Throwable e) {
        this.listener.onException(e);
    }


    private void flightStarted() {
        if (currentFlight != null) {
            flightEnded();
        }

        currentFlight = new Flight();
        currentFlight.setLogbookFile(FlightSimLogController.getInstance().getLogbook().getLogbookFile());
        currentFlight.setId(FlightSimLogController.getInstance().getLogbook().getNextFlightId());

        currentFlight.setFlightSim(FlightSimLogController.getInstance().getLogbook().getDefaultFlightsim());
        currentFlight.setCategory(FlightSimLogController.getInstance().getLogbook().getAutotrackerCategory());
        currentFlight.setComputerDepartureTime(Calendar.getInstance());
        try {
            String file = ClassLoader.getSystemResource("FlightStarted.mp3").toURI().toString();

            Media media = new Media(file);
            MediaPlayer mp = new MediaPlayer(media);

            mp.play();
        } catch (URISyntaxException e) {
            throw new FlightSimLogException(e);
        }

        listener.onFlightStarted();

        inFlight = true;

        flightScreenshotGrabber = new FlightScreenshotGrabber(currentFlight);
        flightScreenshotGrabber.start();

    }


    private void fillPosition(WorldPosition position, Airport airport, SimulationMeasurement measurement) {
        position.setCoordLat(measurement.getCoordLat());
        position.setCoordLon(measurement.getCoordLon());
        position.setCoordAlt(measurement.getCoordAlt());

        if (measurement.isOnGround()) {
            position.setIcao(airport.getIcao());
            position.setDescription(airport.getName());
        } else {
            position.setDescription("Airborne (" + airport.getName() + ")");
        }


    }


    private void flightEnded() {
        flightScreenshotGrabber.setRunning(false);
        flightScreenshotGrabber = null;

        if (currentFlight != null) {


            SimulationData simulationData = currentFlight.getSimulationData();


            // Aircraft
            if (simulationData.getAircraft() != null && !simulationData.getAircraft().isEmpty()) {
                SimAircraft simAircraft = FlightSimLogController.getInstance().getOrCreateSimAircraft(simulationData.getAircraft());
                currentFlight.setSimAircraft(simAircraft);
            }


            if (!simulationData.getMeasurements().isEmpty()) {

                SimulationMeasurement firstMeasurement = simulationData.getMeasurements().getFirst();
                SimulationMeasurement lastMeasurement = simulationData.getMeasurements().getLast();


                Airport departureAirport = AirportsService.getInstance().getClosestAirport(firstMeasurement.getCoordLat(), firstMeasurement.getCoordLon());
                fillPosition(currentFlight.getDeparturePosition(), departureAirport, firstMeasurement);

                Airport arrivalAirport = AirportsService.getInstance().getClosestAirport(lastMeasurement.getCoordLat(), lastMeasurement.getCoordLon());
                fillPosition(currentFlight.getArrivalPosition(), arrivalAirport, lastMeasurement);

                Calendar departureTime = Calendar.getInstance();
                departureTime.setTime(new Date(firstMeasurement.getTime()));
                currentFlight.setDepartureTime(departureTime);

                Calendar arrivalTime = Calendar.getInstance();
                arrivalTime.setTime(new Date(lastMeasurement.getTime()));
                currentFlight.setArrivalTime(arrivalTime);

            }

            currentFlight.setComputerArrivalTime(Calendar.getInstance());

            currentFlight.setDescription(currentFlight.getDeparturePosition().getDescription() + " to " + currentFlight.getArrivalPosition().getDescription());


            try {
                FlightSimLogController.getInstance().saveFlight(currentFlight);
                String file = ClassLoader.getSystemResource("FlightFinished.mp3").toURI().toString();

                Media media = new Media(file);
                MediaPlayer mp = new MediaPlayer(media);

                mp.play();
            } catch (URISyntaxException e) {
                throw new FlightSimLogException(e);
            }

            listener.onFlightEnded(currentFlight.getDescription());

            currentFlight = null;

            inFlight = false;
        }
    }

    public void startFlightManually() {
        flightStarted();
    }

    public void endFlightManually() {
        flightEnded();
    }
}