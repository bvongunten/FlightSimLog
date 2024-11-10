package ch.nostromo.flightsimlog.fxui;


import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTracker;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTrackerListener;
import ch.nostromo.flightsimlog.utils.ClipboardTools;
import ch.nostromo.flightsimlog.utils.GeoJson;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogbookController {

    @FXML
    BorderPane paneFlights;

    @FXML
    ChoiceBox cbCategories;

    @FXML
    Button btnCreateFlight;

    @FXML
    Button btnAutotracker;

    @FXML
    Button btnGeoJsonPath;

    @FXML
    Button btnGeoJsonAirport;

    @FXML
    Button btnCategories;


    Logbook logbook = null;

    Category currentFilter = null;

    public void setLogbook(Logbook logbook, Category currentFilter) {
        this.currentFilter = currentFilter;
        this.logbook = logbook;

        if (logbook != null) {
            setCategoryFilter();
            setTable(logbook.getFilteredFlightList(currentFilter));

            btnAutotracker.setDisable(false);
            btnGeoJsonPath.setDisable(false);
            btnGeoJsonAirport.setDisable(false);
            btnCreateFlight.setDisable(false);
            btnCategories.setDisable(false);
            cbCategories.setDisable(false);
        } else {
            btnAutotracker.setDisable(true);
            btnGeoJsonPath.setDisable(true);
            btnGeoJsonAirport.setDisable(true);
            btnCreateFlight.setDisable(true);
            btnCategories.setDisable(true);
            cbCategories.setDisable(true);
        }

    }

    private void setCategoryFilter() {
        List<Category> filters = new ArrayList<>();
        filters.add(null);
        filters.addAll(logbook.getCategories());

        ObservableList<Category> items = FXCollections.observableArrayList();
        items.addAll(filters);

        cbCategories.setItems(items);
        cbCategories.getSelectionModel().select(currentFilter);

        cbCategories.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Category>() {

            @Override
            public void changed(ObservableValue<? extends Category> observableValue, Category category, Category t1) {
                currentFilter = t1;
                setTable(logbook.getFilteredFlightList(currentFilter));

            }
        });


    }

    private void setTable(List<Flight> flights) {
        ObservableList<Flight> data =
                FXCollections.observableArrayList(flights);

        TableColumn computerDepartureCol = new TableColumn("Computer Time");
        computerDepartureCol.setMinWidth(50);
        computerDepartureCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("formatedComputerDepartureTime"));


        TableColumn descriptionCol = new TableColumn("Description");
        descriptionCol.setMinWidth(250);
        descriptionCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("description"));


        TableColumn categoryCol = new TableColumn("Category");
        categoryCol.setMinWidth(50);
        categoryCol.setCellValueFactory(new PropertyValueFactory<Flight, Category>("category"));


        TableColumn distanceCol = new TableColumn("Distance");
        distanceCol.setMinWidth(50);
        distanceCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("formatedDistance"));


        TableColumn hoursCol = new TableColumn("Duration");
        hoursCol.setMinWidth(50);
        hoursCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("formatedFlightTime"));

        TableColumn aircraftCol = new TableColumn("Aircraft");
        aircraftCol.setMinWidth(50);
        aircraftCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("aircraft"));

        TableColumn fromCol = new TableColumn("From");
        fromCol.setMinWidth(50);
        fromCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("formatedDeparturePositionIcao"));

        TableColumn toCol = new TableColumn("To");
        toCol.setMinWidth(50);
        toCol.setCellValueFactory(new PropertyValueFactory<Flight, String>("formatedArrivalPositionIcao"));

        TableView table = new TableView<Flight>();
        table.setItems(data);
        table.getColumns().addAll(computerDepartureCol, descriptionCol, fromCol, toCol, distanceCol, hoursCol, aircraftCol, categoryCol);

        paneFlights.setCenter(table);

        table.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FlightSimLogController.getInstance().showFlight(row.getItem());
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("Delete a flight");
                    alert.setContentText("Are you sure?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        FlightSimLogController.getInstance().deleteFlight(row.getItem());
                        setTable(logbook.getFilteredFlightList(currentFilter));
                    }


                }
            });
            return row;
        });

    }

    @FXML
    void onCreateFlight(ActionEvent event) {
        FlightSimLogController.getInstance().createFlight();

    }

    @FXML
    void onQuit(ActionEvent event) {
        Platform.exit();
    }


    @FXML
    void onGeoJsonPath(ActionEvent event) {
        String geoJson = GeoJson.createGeoJson(logbook.getFilteredFlightList(currentFilter), true);
        ClipboardTools.pasteToClipboard(geoJson);
        FlightSimLogController.getInstance().showWarning("GeoJson copied to clipboard");
    }

    @FXML
    void onGeoJsonAirport(ActionEvent event) {
        String geoJson = GeoJson.createGeoJson(logbook.getFilteredFlightList(currentFilter), false);
        ClipboardTools.pasteToClipboard(geoJson);
        FlightSimLogController.getInstance().showWarning("GeoJson copied to clipboard");
    }

    @FXML
    void onLoad(ActionEvent event) {
        this.logbook = FlightSimLogController.getInstance().loadLogbookManually();
        this.setLogbook(logbook, currentFilter);
    }

    @FXML
    void onCategories(ActionEvent event) {
        FlightSimLogController.getInstance().showCategories();
        FlightSimLogController.getInstance().saveLogbookToFile();
        this.setLogbook(logbook, currentFilter);

    }

    @FXML
    void onSettings(ActionEvent event) {
        FlightSimLogController.getInstance().showSettings();
    }

    @FXML
    void onNewLogbook(ActionEvent event) {
        this.logbook =  FlightSimLogController.getInstance().newLogbookManually();
        this.setLogbook(logbook, currentFilter);
    }

    @FXML
    void onAutotracker(ActionEvent event) {
        AutoTracker autoTracker = new AutoTracker(new AutoTrackerListener() {
            @Override
            public void onFlightStarted() {
                // nothing
            }

            @Override
            public void onFlightEnded() {
                Platform.runLater(() -> {
                    setLogbook(logbook, currentFilter);
                });

            }
        });

        Service<Void> service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        autoTracker.startTracker();
                        return null;
                    }
                };
            }
        };
        service.setOnFailed(evt -> {
            FlightSimLogController.getInstance().showError(service.getException());
            service.getException().printStackTrace(System.err);
        });

        service.start();
        FlightSimLogController.getInstance().showWarning("Running auto tracker ...");

        try {
            autoTracker.stopTracker();
        } catch ( Exception ignored ) {
            // tried our best ;)
        }

        this.setLogbook(this.logbook, this.currentFilter);

    }

}
