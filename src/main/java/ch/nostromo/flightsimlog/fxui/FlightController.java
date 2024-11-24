package ch.nostromo.flightsimlog.fxui;


import ch.nostromo.flightsimlog.FlightSimLogConfig;
import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.airports.Airport;
import ch.nostromo.flightsimlog.airports.AirportsService;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.FlightSim;
import ch.nostromo.flightsimlog.data.coordinates.SimulationMeasurement;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.data.flight.SimulationData;
import ch.nostromo.flightsimlog.tracker.TrackerData;
import ch.nostromo.flightsimlog.tracker.TrackerListener;
import ch.nostromo.flightsimlog.tracker.simconnect.SimConnectTracker;
import ch.nostromo.flightsimlog.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class FlightController {

    @FXML
    private ChoiceBox<Category> cbCategories;
    @FXML
    private ChoiceBox<FlightSim> cbFlightSim;

    @FXML
    private TextField txtDescription;

    @FXML
    private ComboBox<SimAircraft> cbAircraft;

    @FXML
    private TextField txtDepartureICAO;

    @FXML
    private TextField txtDepartureDescription;

    @FXML
    private TextField txtDepartureLon;

    @FXML
    private TextField txtDepartureLat;

    @FXML
    private TextField txtDepartureTime;


    @FXML
    private TextField txtArrivalICAO;

    @FXML
    private TextField txtArrivalDescription;

    @FXML
    private TextField txtArrivalLon;

    @FXML
    private TextField txtArrivalLat;

    @FXML
    private TextField txtArrivalTime;


    @FXML
    private TextField txtComputerDepartureTime;

    @FXML
    private TextField txtComputerArrivalTime;

    @FXML
    private CheckBox cbRealTime;


    @FXML
    private TextField txtId;

    @FXML
    private TextField txtDistance;

    @FXML
    private TextField txtDuration;

    @FXML
    private TextField txtSimulationMeasurements;

    @FXML
    private TextField txtImagesCount;

    @FXML
    private Button btnTracker;

    @FXML
    private Button btnImageGrabber;


    Flight flight;

    SimConnectTracker simConnectTracker;
    ScreenshotGrabber screenshotGrabber;

    @FXML
    void onCancel(ActionEvent event) {
        closeForm();
    }


    public void setFlight(Flight flight) {
        this.flight = flight;

        // Fill lists
        ObservableList<Category> categoriesList = FXCollections.observableArrayList();
        categoriesList.addAll(FlightSimLogController.getInstance().getCategories());
        cbCategories.setItems(categoriesList);
        cbFlightSim.getItems().setAll(FlightSim.values());
        cbAircraft.setEditable(true);
        cbAircraft.getEditor().setId("txtAircraft");

        for (SimAircraft simAircraft : FlightSimLogController.getInstance().getSimAircraft()) {
            cbAircraft.getItems().add(simAircraft);
        }


        // Fill flight
        this.cbCategories.getSelectionModel().select(flight.getCategory());
        this.cbFlightSim.getSelectionModel().select(flight.getFlightSim());

        if (flight.getSimAircraft() != null) {
            this.cbAircraft.getSelectionModel().select(flight.getSimAircraft());
        } else {
            this.cbAircraft.getEditor().setText("");
        }

        this.txtDescription.setText(flight.getDescription());

        this.txtDepartureICAO.setText(flight.getDeparturePosition().getIcao());
        this.txtDepartureLat.setText(String.valueOf(flight.getDeparturePosition().getCoordLat()));
        this.txtDepartureLon.setText(String.valueOf(flight.getDeparturePosition().getCoordLon()));
        this.txtDepartureDescription.setText(flight.getDeparturePosition().getDescription());
        this.txtDepartureTime.setText(CalendarTools.calToString(flight.getDepartureTime()));

        this.txtArrivalICAO.setText(flight.getArrivalPosition().getIcao());
        this.txtArrivalLat.setText(String.valueOf(flight.getArrivalPosition().getCoordLat()));
        this.txtArrivalLon.setText(String.valueOf(flight.getArrivalPosition().getCoordLon()));
        this.txtArrivalDescription.setText(flight.getArrivalPosition().getDescription());
        this.txtArrivalTime.setText(CalendarTools.calToString(flight.getArrivalTime()));

        this.txtComputerDepartureTime.setText(CalendarTools.calToString(flight.getComputerDepartureTime()));
        this.txtComputerArrivalTime.setText(CalendarTools.calToString(flight.getComputerArrivalTime()));
        this.cbRealTime.setSelected(flight.getRealTime());

        this.txtId.setText(flight.getId());

        addChangeListeners(txtDepartureLat, txtDepartureLon, txtDepartureTime, txtArrivalLon, txtArrivalLat, txtArrivalTime, txtComputerArrivalTime, txtDepartureTime);

        this.updateFlightInfos();

    }

    private void addChangeListeners(TextField... textFields) {
        for (TextField textField : textFields) {
            textField.textProperty().addListener((obs, old, niu) -> {
                updateFlightInfos();
            });
        }
    }


    private void updateFlightInfos() {

        // Distance
        if (!txtDepartureLat.getText().isEmpty() && !txtDepartureLon.getText().isEmpty() && !txtArrivalLat.getText().isEmpty() && !txtArrivalLon.getText().isEmpty()) {
            double departureLat = Double.parseDouble(txtDepartureLat.getText());
            double departureLon = Double.parseDouble(txtDepartureLon.getText());
            double arrivalLat = Double.parseDouble(txtArrivalLat.getText());
            double arrivalLon = Double.parseDouble(txtArrivalLon.getText());
            DecimalFormat df = new DecimalFormat("####0.00");
            txtDistance.setText(df.format(GeoTools.calculateDistance(departureLat, departureLon, arrivalLat, arrivalLon, "N")));
        }

        // Duration
        Calendar departureTime = CalendarTools.stringToCal(txtDepartureTime.getText());
        Calendar arrivalTime = CalendarTools.stringToCal(txtArrivalTime.getText());

        if (departureTime != null && arrivalTime != null) {
            long seconds = (arrivalTime.getTimeInMillis() - departureTime.getTimeInMillis()) / 1000;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;

            txtDuration.setText(String.format("%02d:%02d", hours, minutes));
        }

        txtImagesCount.setText(String.valueOf(flight.getImagesCount()));
        txtSimulationMeasurements.setText(String.valueOf(flight.getSimulationData().getMeasurements().size()));

    }


    private boolean validateFieldsForMissingInformation(TextField... txtFields) {
        for (TextField textField : txtFields) {
            if (textField.getText().isEmpty()) {
                FlightSimLogController.getInstance().showWarning(textField.getId() + " cannot be empty");
                textField.requestFocus();
                return false;
            }
        }
        return true;
    }


    @FXML
    void onSaveFlight(ActionEvent event) {
        // Check input
        if (!validateFieldsForMissingInformation(cbAircraft.getEditor(), txtDescription, txtArrivalDescription, txtArrivalLat, txtArrivalLon, txtArrivalTime, txtDepartureDescription, txtDepartureLat, txtDepartureLon, txtComputerDepartureTime, txtComputerDepartureTime)) {
            return;
        }

        try {

            flight.setCategory(cbCategories.getSelectionModel().getSelectedItem());
            flight.setFlightSim(cbFlightSim.getSelectionModel().getSelectedItem());

            flight.setDescription(txtDescription.getText());

            flight.getDeparturePosition().setIcao(txtDepartureICAO.getText());
            flight.getDeparturePosition().setDescription(txtDepartureDescription.getText());
            flight.getDeparturePosition().setCoordLat(Double.parseDouble(txtDepartureLat.getText()));
            flight.getDeparturePosition().setCoordLon(Double.parseDouble(txtDepartureLon.getText()));
            flight.setDepartureTime(CalendarTools.stringToCal(txtDepartureTime.getText(), false));

            flight.getArrivalPosition().setIcao(txtArrivalICAO.getText());
            flight.getArrivalPosition().setDescription(txtArrivalDescription.getText());
            flight.getArrivalPosition().setCoordLat(Double.parseDouble(txtArrivalLat.getText()));
            flight.getArrivalPosition().setCoordLon(Double.parseDouble(txtArrivalLon.getText()));
            flight.setArrivalTime(CalendarTools.stringToCal(txtArrivalTime.getText(), false));

            flight.setComputerDepartureTime(CalendarTools.stringToCal(txtComputerDepartureTime.getText(), false));
            flight.setComputerArrivalTime(CalendarTools.stringToCal(txtComputerArrivalTime.getText(), false));
            flight.setRealTime(cbRealTime.isSelected());

            flight.setSimAircraft(FlightSimLogController.getInstance().getOrCreateSimAircraft(cbAircraft.getEditor().getText()));

        } catch (Exception e) {
            FlightSimLogController.getInstance().showWarning("Bind failed with error: " + e.getMessage());
            return;
        }

        try {
            FlightSimLogController.getInstance().saveFlight(flight);
        } catch (FlightSimLogException e) {
            FlightSimLogController.getInstance().showWarning("Save failed with error: " + e.getMessage());
            return;
        }

        closeForm();
    }

    @FXML
    void onDepartureICAOLookup(ActionEvent event) {

        if (!txtDepartureICAO.getText().isEmpty()) {
            Airport airport = AirportsService.getInstance().getAirportByIcao(txtDepartureICAO.getText());

            if (airport != null) {
                txtDepartureICAO.setText(airport.getIcao());
                txtDepartureDescription.setText(airport.getName());
                txtDepartureLat.setText(String.valueOf(airport.getCoordinates().getCoordLat()));
                txtDepartureLon.setText(String.valueOf(airport.getCoordinates().getCoordLon()));
            }

        }

        updateFlightInfos();


    }

    @FXML
    void onArrivalICAOLookup(ActionEvent event) {

        if (!txtArrivalICAO.getText().isEmpty()) {
            Airport airport = AirportsService.getInstance().getAirportByIcao(txtArrivalICAO.getText());

            if (airport != null) {
                txtArrivalICAO.setText(airport.getIcao());
                txtArrivalDescription.setText(airport.getName());
                txtArrivalLat.setText(String.valueOf(airport.getCoordinates().getCoordLat()));
                txtArrivalLon.setText(String.valueOf(airport.getCoordinates().getCoordLon()));
            }
        }
        updateFlightInfos();

    }


    @FXML
    void onDepartureNrst(ActionEvent event) {
        if (!txtDepartureLat.getText().isEmpty() && !txtDepartureLon.getText().isEmpty()) {
            double departureLat = Double.parseDouble(txtDepartureLat.getText());
            double departureLon = Double.parseDouble(txtDepartureLon.getText());

            Airport closestAirport = AirportsService.getInstance().getClosestAirport(departureLat, departureLon);
            txtDepartureICAO.setText(closestAirport.getIcao());
            txtDepartureDescription.setText(closestAirport.getName());
        }
    }

    @FXML
    void onArrivalNrst(ActionEvent event) {
        if (!txtArrivalLat.getText().isEmpty() && !txtArrivalLon.getText().isEmpty()) {
            double departureLat = Double.parseDouble(txtArrivalLat.getText());
            double departureLon = Double.parseDouble(txtArrivalLon.getText());

            Airport closestAirport = AirportsService.getInstance().getClosestAirport(departureLat, departureLon);
            txtArrivalICAO.setText(closestAirport.getIcao());
            txtArrivalDescription.setText(closestAirport.getName());
        }

    }


    @FXML
    void onGetGeoJson(ActionEvent event) throws UnsupportedEncodingException {
        String geoJson = GeoJson.createGeoJson(flight, true);
        ClipboardTools.pasteToClipboard(geoJson);
        FlightSimLogController.getInstance().showWarning("GeoJson copied to clipboard");
    }

    @FXML
    void onImageToggle(ActionEvent event) {

        if (screenshotGrabber == null) {
            screenshotGrabber = new ScreenshotGrabber(flight);
            screenshotGrabber.start();
            btnImageGrabber.setText("Stop Image Grabber");
        } else {
            screenshotGrabber.setRunning(false);
            screenshotGrabber = null;
            btnImageGrabber.setText("Start Image Grabber");
        }

    }

    @FXML
    void onSimconnectToggle(ActionEvent event) {

        if (simConnectTracker == null) {
            simConnectTracker = new SimConnectTracker(FlightSimLogConfig.getSimConnectHost(), FlightSimLogConfig.getSimConnectPort(), new TrackerListener() {
                @Override
                public void onData(TrackerData data) {
                    flight.getSimulationData().addTrackerData(data);
                    updateFlightInfos();
                }

                @Override
                public void onEventFileLoaded(String file) {

                }

                @Override
                public void onEventPause(int pause) {

                }

                @Override
                public void onEventSimStart(int start) {

                }

                @Override
                public void onEventSimStop(int stop) {

                }

                @Override
                public void onEventSim(int sim) {

                }

                @Override
                public void onConnected() {

                }

                @Override
                public void onDisconnected() {

                }

                @Override
                public void onException(Throwable e) {

                }
            });

            simConnectTracker.startTracker();

            this.btnTracker.setText("Stop FS tracker");

        } else {
            simConnectTracker.stopTracker();
            simConnectTracker = null;

            this.btnTracker.setText("Start FS tracker");
        }

    }

    @FXML
    void onDepartureTimeNow(ActionEvent event) {
        txtDepartureTime.setText(CalendarTools.calToString(Calendar.getInstance()));
    }

    @FXML
    void onArrivalTimeNow(ActionEvent event) {
        txtArrivalTime.setText(CalendarTools.calToString(Calendar.getInstance()));
    }

    @FXML
    void onDepartureComputerTimeNow(ActionEvent event) {
        txtComputerDepartureTime.setText(CalendarTools.calToString(Calendar.getInstance()));
    }

    @FXML
    void onArrivalCopmputerTimeNow(ActionEvent event) {
        txtComputerArrivalTime.setText(CalendarTools.calToString(Calendar.getInstance()));
    }


    @FXML
    void onSimulationDataMerge(ActionEvent event) {
        SimulationData data = flight.getSimulationData();

        // Aircraft
        if (data.getAircraft() != null && !data.getAircraft().isEmpty()) {
            cbAircraft.getEditor().setText(data.getAircraft());
        }

        // Departure
        if (!data.getGpsPrev().isEmpty()) {
            txtDepartureICAO.setText(data.getGpsPrev());
            onDepartureICAOLookup(null);
        }

        if (!data.getGpsNext().isEmpty()) {
            txtArrivalICAO.setText(data.getGpsNext());
            onArrivalICAOLookup(null);
        }

        if (!data.getMeasurements().isEmpty()) {

            SimulationMeasurement firstMeasurement = data.getMeasurements().getFirst();

            txtDepartureLat.setText(String.valueOf(firstMeasurement.getCoordLat()));
            txtDepartureLon.setText(String.valueOf(firstMeasurement.getCoordLon()));

            Calendar departureTime = Calendar.getInstance();
            departureTime.setTime(new Date(firstMeasurement.getTime()));
            txtDepartureTime.setText(CalendarTools.calToString(departureTime));


            SimulationMeasurement lastMeasurement = data.getMeasurements().getLast();

            txtArrivalLat.setText(String.valueOf(lastMeasurement.getCoordLat()));
            txtArrivalLon.setText(String.valueOf(lastMeasurement.getCoordLon()));

            Calendar arrivalTime = Calendar.getInstance();
            arrivalTime.setTime(new Date(lastMeasurement.getTime()));
            txtArrivalTime.setText(CalendarTools.calToString(arrivalTime));

        }
            
        
    }



    public void closeForm() {

        try {
            if (simConnectTracker != null) {
                simConnectTracker.stopTracker();
                simConnectTracker = null;
            }
        } catch (Exception e) {
            // Nah ...
        }

        try {
            if (screenshotGrabber != null) {
                screenshotGrabber.setRunning(false);
                screenshotGrabber = null;
            }
        } catch (Exception e) {
            // Nah ...
        }

        // Close frame
        Stage stage = (Stage) txtId.getScene().getWindow();
        stage.close();

        FlightSimLogController.getInstance().showLogBook();

    }
}
