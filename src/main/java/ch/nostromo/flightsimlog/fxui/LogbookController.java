package ch.nostromo.flightsimlog.fxui;


import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.fxui.dialogs.AutotrackerDialog;
import ch.nostromo.flightsimlog.fxui.dialogs.TextMessageDialog;
import ch.nostromo.flightsimlog.fxui.fxutils.CheckBoxCellFactory;
import ch.nostromo.flightsimlog.fxui.fxutils.TableViewResizer;
import ch.nostromo.flightsimlog.reports.Reports;
import ch.nostromo.flightsimlog.utils.GeoJson;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogbookController {

    @FXML
    public Button btnAircraft;


    @FXML
    BorderPane paneFlights;

    @FXML
    ChoiceBox<Category> cbCategories;

    @FXML
    Button btnCreateFlight;

    @FXML
    Button btnAutoTracker;

    @FXML
    Button btnGeoJsonPath;

    @FXML
    Button btnGeoJsonAirport;

    @FXML
    Button btnCategories;

    @FXML
    Button btnReports;

    Logbook logbook = null;

    Category currentFilter = null;

    public void setLogbook(Logbook logbook, Category currentFilter) {
        this.currentFilter = currentFilter;
        this.logbook = logbook;

        if (logbook != null) {
            setCategoryFilter();
            setTable(logbook.getFilteredFlightList(currentFilter));
            btnAircraft.setDisable(false);
            btnAutoTracker.setDisable(false);
            btnGeoJsonPath.setDisable(false);
            btnGeoJsonAirport.setDisable(false);
            btnCreateFlight.setDisable(false);
            btnCategories.setDisable(false);
            btnReports.setDisable(false);
            cbCategories.setDisable(false);
        } else {
            btnAircraft.setDisable(true);
            btnAutoTracker.setDisable(true);
            btnGeoJsonPath.setDisable(true);
            btnGeoJsonAirport.setDisable(true);
            btnCreateFlight.setDisable(true);
            btnCategories.setDisable(true);
            btnReports.setDisable(false);
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

        cbCategories.getSelectionModel().selectedItemProperty().addListener((observableValue, category, t1) -> {
            currentFilter = t1;
            setTable(logbook.getFilteredFlightList(currentFilter));

        });


    }

    private void setTable(List<Flight> flights) {
        ObservableList<Flight> data = FXCollections.observableArrayList(flights);
        TableView<Flight> table = new TableView<>();
        table.setItems(data);

        TableColumn<Flight, String> pcDepartureTime = new TableColumn<>("Computer Time");
        pcDepartureTime.setCellValueFactory(flightStringCellDataFeatures -> new ReadOnlyObjectWrapper<>(new SimpleDateFormat("yyyy.MM.dd HH:mm").format(flightStringCellDataFeatures.getValue().getComputerDepartureTime().getTime())));
        table.getColumns().add(pcDepartureTime);

        TableColumn<Flight, Category> category = new TableColumn<>("Category");
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        table.getColumns().add(category);


        TableColumn<Flight, String> description = new TableColumn<>("Description");
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().add(description);


        TableColumn<Flight, Double> distance = new TableColumn<>("Distance");
        distance.setCellValueFactory(flightDoubleCellDataFeatures -> {
            DecimalFormat df = new DecimalFormat("####0.00");
            return new ReadOnlyObjectWrapper<>(Double.valueOf(df.format(flightDoubleCellDataFeatures.getValue().getCalculatedDistanceInNm())));
        });
        table.getColumns().add(distance);

        TableColumn<Flight, String> duration = new TableColumn<>("Duration");
        duration.setCellValueFactory(flightDoubleCellDataFeatures -> {
            long seconds = flightDoubleCellDataFeatures.getValue().getCalculatedDurationInSec();

            // Calculate hours and minutes
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;

            // Format and return the string as "HH:MM"
            return new ReadOnlyObjectWrapper<>(String.format("%02d:%02d", hours, minutes));

        });
        table.getColumns().add(duration);

        TableColumn<Flight, String> aircraft = new TableColumn<>("Aircraft");
        aircraft.setCellValueFactory(new PropertyValueFactory<>("simAircraft"));
        table.getColumns().add(aircraft);

        TableColumn<Flight, String> from = new TableColumn<>("From");
        from.setCellValueFactory(flightDoubleCellDataFeatures -> new ReadOnlyObjectWrapper<>(flightDoubleCellDataFeatures.getValue().getDeparturePosition().getIcao()));
        table.getColumns().add(from);

        TableColumn<Flight, String> to = new TableColumn<>("To");
        to.setCellValueFactory(flightDoubleCellDataFeatures -> new ReadOnlyObjectWrapper<>(flightDoubleCellDataFeatures.getValue().getArrivalPosition().getIcao()));
        table.getColumns().add(to);

        TableColumn<Flight, String> imgCount = new TableColumn<>("Images");
        imgCount.setCellValueFactory(new PropertyValueFactory<>("imagesCount"));
        table.getColumns().add(imgCount);

        TableColumn<Flight, Boolean> mastered = new TableColumn<>("RealTime");
        mastered.setCellValueFactory(new PropertyValueFactory<>("realTime"));
        mastered.setCellFactory(new CheckBoxCellFactory<>());
        table.getColumns().add(mastered);


        paneFlights.setCenter(table);

        table.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FlightSimLogController.getInstance().showAndWaitFlight(row.getItem());

                    setTable(logbook.getFilteredFlightList(currentFilter));


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
        closeForm();
    }


    @FXML
    void onGeoJsonPath(ActionEvent event) {
        String geoJson = GeoJson.createGeoJson(logbook.getFilteredFlightList(currentFilter), true, true);
        new TextMessageDialog((Stage) btnGeoJsonPath.getScene().getWindow(), "GeoJson", geoJson).showAndWait();
    }

    @FXML
    void onGeoJsonAirport(ActionEvent event) {
        String geoJson = GeoJson.createGeoJson(logbook.getFilteredFlightList(currentFilter), false, false);
        new TextMessageDialog((Stage) btnGeoJsonAirport.getScene().getWindow(), "GeoJson", geoJson).showAndWait();
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
    void onReports(ActionEvent event) {
        Reports.createReports(logbook);
    }

    @FXML
    void onNewLogbook(ActionEvent event) {
        this.logbook = FlightSimLogController.getInstance().newLogbookManually();
        this.setLogbook(logbook, currentFilter);
    }

    @FXML
    void onAutotracker(ActionEvent event) {
        AutotrackerDialog atd = new AutotrackerDialog(null);
        atd.showAndWait();

        this.setLogbook(this.logbook, this.currentFilter);

    }

    public void closeForm() {

        FlightSimLogController.getInstance().shutDown();
    }
}
