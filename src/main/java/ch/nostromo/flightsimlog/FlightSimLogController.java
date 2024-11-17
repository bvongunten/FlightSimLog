package ch.nostromo.flightsimlog;

import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Aircraft;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.fxui.*;
import ch.nostromo.flightsimlog.fxui.dialogs.CategoriesDialog;
import ch.nostromo.flightsimlog.fxui.dialogs.SettingsDialog;
import ch.nostromo.flightsimlog.utils.ClipboardTools;
import ch.nostromo.flightsimlog.utils.LogBookTools;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Getter;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

public class FlightSimLogController {

    @Getter
    static FlightSimLogController instance;

    @Getter
    Logbook logbook = null;

    Stage primaryStage;

    Category currentFilter = null;

    Preferences preferences;

    public static void createInstance(Stage primaryStage) {
        instance = new FlightSimLogController(primaryStage);
        instance.initializeAndLoadLogbook();
    }

    public FlightSimLogController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        preferences = Preferences.userNodeForPackage(FlightSimLog.class);
    }

    public void initializeAndLoadLogbook() {
        String logBookFileName = FlightSimLogConfig.getLogbookFile();

       File logBookFile = null;
        if (logBookFileName != null) {
            logBookFile = new File(logBookFileName);
        }

        // Check again ;)
        if (logBookFile != null && logBookFile.exists()) {
            logbook = LogBookTools.loadXml(logBookFile);
        }

        // **** AIRCRAFT GENERATION *****

        logbook.getAircraft().clear();
        for (SimAircraft simAircraft : logbook.getSimAircraft()) {
            Aircraft aircraft = new Aircraft();
            aircraft.setId(logbook.getNextAircraftId());
            aircraft.setDescription(simAircraft.getDescription());
            aircraft.getSimAircraft().add(simAircraft);
            logbook.getAircraft().add(aircraft);
        }


        saveLogbookToFile();

    }

    public Logbook loadLogbookManually() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        File logBooktoLoad = new File(selectedDirectory, "logbook.xml");

        if (!logBooktoLoad.exists()) {
            showWarning("No logbook found in directory!");
            return null;
        }

        logbook = LogBookTools.loadXml(logBooktoLoad);
        FlightSimLogConfig.putLogbookFile(logBooktoLoad.getAbsolutePath());

        return logbook;
    }


    public Logbook newLogbookManually() {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        File logBooktoCreate = new File(selectedDirectory, "logbook.xml");

        if (logBooktoCreate.exists()) {
            showWarning("Logbook already exists in the directory!");
            return null;
        }

        logbook = new Logbook();
        logbook.setLogbookFile(logBooktoCreate);

        Category autoTracker = new Category();
        autoTracker.setId(logbook.getNextCategoryId());
        autoTracker.setDescription("Auto tracker");

        logbook.getCategories().add(autoTracker);

        saveLogbookToFile();
        FlightSimLogConfig.putLogbookFile(logBooktoCreate.getAbsolutePath());

        return logbook;
    }


    public void saveLogbookToFile() {
        LogBookTools.saveXml(logbook);
    }

    public List<Category> getCategories() {
        return logbook.getCategories();
    }

    public List<SimAircraft> getSimAircraft() {
        return logbook.getSortedSimAircraft();
    }

    public SimAircraft getOrCreateSimAircraft(String simAircraftName) {
        if (simAircraftName == null || simAircraftName.isEmpty()) {
            throw new IllegalArgumentException("Aircraft name cannot be null or empty");
        }

        for (SimAircraft simAircraft : logbook.getSimAircraft()) {
            if (simAircraft.getDescription().equalsIgnoreCase(simAircraftName)) {
                return simAircraft;
            }
        }

        SimAircraft result = new SimAircraft();
        result.setId(logbook.getNextSimAircraftId());
        result.setDescription(simAircraftName);

        logbook.getSimAircraft().add(result);

        return result;
    }

    public void createFlight() {
        Flight flight = new Flight();
        flight.setId(logbook.getNextFlightId());
        flight.setLogbookFile(logbook.getLogbookFile());

        flight.setFlightSim(logbook.getDefaultFlightsim());
        flight.setCategory(logbook.getCategories().get(0));
        flight.setComputerDepartureTime(Calendar.getInstance());

        showFlight(flight);
    }

    public void deleteFlight(Flight flight) {
        logbook.getFlights().remove(flight);
        flight.deleteSimulationData();
        saveLogbookToFile();
    }

    public void saveFlight(Flight flightToSave) {
        if (logbook.getFlights().contains(flightToSave)) {
            Flight target = logbook.getFlightById(flightToSave.getId());
            target.merge(flightToSave);
            target.saveSimulationData();
        } else {
            logbook.getFlights().add(flightToSave);
            flightToSave.saveSimulationData();
        }

        saveLogbookToFile();
    }

    public void showLogBook() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FlightSimLogController.class.getResource("/fxml/Logbook.fxml"));
            Parent parent = fxmlLoader.load();
            LogbookController controller = fxmlLoader.<LogbookController>getController();

            controller.setLogbook(logbook, currentFilter);

            primaryStage.setTitle("FlightSimLog " + logbook.getLogbookFile().getAbsolutePath());
            primaryStage.setScene(new Scene(parent));
            primaryStage.show();
        } catch (Exception e) {
            showError(e);
        }
    }

    public void showAircraftList() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FlightSimLogController.class.getResource("/fxml/AircraftList.fxml"));
            Parent parent = fxmlLoader.load();
            AircraftListController controller = fxmlLoader.<AircraftListController>getController();

            controller.setup(logbook);

            primaryStage.setTitle("Aircraft List");
            primaryStage.setScene(new Scene(parent));
            primaryStage.show();
        } catch (Exception e) {
            showError(e);
        }
    }


    public void showFlight(Flight flight) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FlightSimLogController.class.getResource("/fxml/Flight.fxml"));
            Parent parent = fxmlLoader.load();
            FlightController controller = fxmlLoader.<FlightController>getController();

            // Edit copy
            controller.setFlight(new Flight(flight));

            primaryStage.setTitle("Flight");
            primaryStage.setScene(new Scene(parent));
            primaryStage.show();
        } catch (Exception e) {
            showError(e);
        }
    }


    public void showSettings() {

        Optional<Pair<String, String>> config = new SettingsDialog(FlightSimLogConfig.getSimConnectHost(), String.valueOf(FlightSimLogConfig.getSimConnectPort())).showAndWait();

        config.ifPresent(pair -> {
            FlightSimLogConfig.putSimConnectHost(pair.getKey());
            FlightSimLogConfig.putSimConnectPort(Integer.parseInt(pair.getValue()));
        });
    }

    public void showCategories() {
        CategoriesDialog dialog = new CategoriesDialog(logbook.getCategories());
        dialog.showAndWait();
    }

    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showError(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText("An Error Occurred");
        alert.setContentText(ex.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);

        alert.setHeight(800);
        alert.setWidth(1200);

        alert.showAndWait();
    }


    public void showTextDialog(String text) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle("Text Viewer");

        TextArea textArea = new TextArea();
        textArea.setText(text);
        textArea.setEditable(false);
        textArea.setWrapText(false);

        VBox.setVgrow(textArea, Priority.ALWAYS);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialogStage.close());

        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            ClipboardTools.pasteToClipboard(textArea.getText());
        });

        VBox layout = new VBox(10, textArea, copyButton, closeButton);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 800, 400); // Set dialog dimensions to 800x400
        dialogStage.setScene(scene);

        // Show the dialog
        dialogStage.show();
    }


    public void showAircraft(Aircraft item) {
    }

    public void deleteAircraft(Aircraft item) {
    }


}
