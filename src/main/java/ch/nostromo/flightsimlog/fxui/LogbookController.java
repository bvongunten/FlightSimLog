package ch.nostromo.flightsimlog.fxui;


import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.fxui.fxutils.TableViewResizer;
import ch.nostromo.flightsimlog.statistics.Statistics;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTracker;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTrackerListener;
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

    @FXML
    Button btnStatistics;

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
            btnStatistics.setDisable(false);
            cbCategories.setDisable(false);
        } else {
            btnAutotracker.setDisable(true);
            btnGeoJsonPath.setDisable(true);
            btnGeoJsonAirport.setDisable(true);
            btnCreateFlight.setDisable(true);
            btnCategories.setDisable(true);
            btnStatistics.setDisable(false);
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
        ObservableList<Flight> data = FXCollections.observableArrayList(flights);
        TableView<Flight> table = new TableView<>();
        table.setItems(data);

        TableColumn<Flight, String> pcDepartureTime = new TableColumn<>("Computer Time");
        pcDepartureTime.setCellValueFactory(new PropertyValueFactory<>("formatedComputerDepartureTime"));
        table.getColumns().add(pcDepartureTime);

        TableColumn<Flight, String> description = new TableColumn<>("Description");
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().add(description);


        TableColumn<Flight, Category> category = new TableColumn<>("Category");
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        table.getColumns().add(category);

        TableColumn<Flight, Double> distance = new TableColumn<>("Distance");
        distance.setCellValueFactory(new PropertyValueFactory<>("formatedDistance"));
        table.getColumns().add(distance);

        TableColumn<Flight, String> duration = new TableColumn<>("Duration");
        duration.setCellValueFactory(new PropertyValueFactory<>("formatedFlightTime"));
        table.getColumns().add(duration);

        TableColumn<Flight, String> aircraft = new TableColumn<>("Aircraft");
        aircraft.setCellValueFactory(new PropertyValueFactory<>("simAircraft"));
        table.getColumns().add(aircraft);

        TableColumn<Flight, String> from = new TableColumn<>("From");
        from.setCellValueFactory(new PropertyValueFactory<>("formatedDeparturePositionIcao"));
        table.getColumns().add(from);

        TableColumn<Flight, String> to = new TableColumn<>("To");
        to.setCellValueFactory(new PropertyValueFactory<>("formatedArrivalPositionIcao"));
        table.getColumns().add(to);


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

        TableViewResizer.autoSizeColumns(table);

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
        FlightSimLogController.getInstance().showTextDialog(geoJson);
    }

    @FXML
    void onGeoJsonAirport(ActionEvent event) {
        String geoJson = GeoJson.createGeoJson(logbook.getFilteredFlightList(currentFilter), false);
        FlightSimLogController.getInstance().showTextDialog(geoJson);
    }

    @FXML
    void onAircraft(ActionEvent event) {
        FlightSimLogController.getInstance().showAircraftList();
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
    void onStatistics(ActionEvent event) {
        Statistics.createStatistics(logbook.getFilteredFlightList(currentFilter));
    }

    @FXML
    void onNewLogbook(ActionEvent event) {
        this.logbook = FlightSimLogController.getInstance().newLogbookManually();
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
        } catch (Exception ignored) {
            // tried our best ;)
        }

        this.setLogbook(this.logbook, this.currentFilter);

    }

}
