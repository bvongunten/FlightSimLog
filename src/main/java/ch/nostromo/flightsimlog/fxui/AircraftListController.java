package ch.nostromo.flightsimlog.fxui;

import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Aircraft;
import ch.nostromo.flightsimlog.data.base.AircraftSeatingType;
import ch.nostromo.flightsimlog.data.base.AircraftType;
import ch.nostromo.flightsimlog.data.base.SimAircraft;
import ch.nostromo.flightsimlog.fxui.dialogs.TextMessageDialog;
import ch.nostromo.flightsimlog.fxui.fxutils.CheckBoxCellFactory;
import ch.nostromo.flightsimlog.fxui.fxutils.TableViewResizer;
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

import java.util.List;
import java.util.Optional;

public class AircraftListController {

    @FXML
    BorderPane aircraftListPane;

    @FXML
    TextField txtFilter;

    @FXML
    ChoiceBox<AircraftType> cbFilterType;

    @FXML
    ChoiceBox<AircraftSeatingType> cbFilterSeating;

    @FXML
    CheckBox cbShowOutdated;

    Logbook logbook;

    String currentFilter = "";
    AircraftType currentFilterAircraftType;
    AircraftSeatingType currentFilterSeatingType;

    public void setup(Logbook logbook) {
        this.logbook = logbook;

        txtFilter.textProperty().addListener((obs, old, niu) -> {
           currentFilter = txtFilter.getText();
           setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));
        });

        cbFilterType.getItems().add(null);
        cbFilterType.getItems().addAll(AircraftType.values());
        cbFilterType.valueProperty().addListener((observableValue, aircraftType, t1) -> {
            currentFilterAircraftType = t1;
            setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));
        });

        cbFilterSeating.getItems().add(null);
        cbFilterSeating.getItems().addAll(AircraftSeatingType.values());
        cbFilterSeating.valueProperty().addListener((observableValue, aircraftType, t1) -> {
            currentFilterSeatingType = t1;
            setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));
        });

        cbShowOutdated.selectedProperty().addListener((observable, oldValue, newValue) -> setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected())));

        setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));

    }

    private void setTable(List<Aircraft> flights) {
        ObservableList<Aircraft> data = FXCollections.observableArrayList(flights);

        TableView<Aircraft> table = new TableView<>();
        table.setItems(data);

        TableColumn<Aircraft, String> manufacturer = new TableColumn<>("Manufacturer");
        manufacturer.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        table.getColumns().add(manufacturer);

        TableColumn<Aircraft, String> description = new TableColumn<>("Aircraft");
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        table.getColumns().add(description);

        TableColumn<Aircraft, AircraftType> type = new TableColumn<>("Type");
        type.setCellValueFactory(new PropertyValueFactory<>("aircraftType"));
        table.getColumns().add(type);

        TableColumn<Aircraft, AircraftType> propulsion = new TableColumn<>("Propulsion");
        propulsion.setCellValueFactory(new PropertyValueFactory<>("aircraftPropulsion"));
        table.getColumns().add(propulsion);

//        TableColumn<Aircraft, Integer> engines = new TableColumn<>("Engines");
//        engines.setCellValueFactory(new PropertyValueFactory<>("engines"));
//        table.getColumns().add(engines);

//        TableColumn<Aircraft, Integer> seats = new TableColumn<>("Seats");
//        seats.setCellValueFactory(new PropertyValueFactory<>("seats"));
//        table.getColumns().add(seats);

        TableColumn<Aircraft, AircraftSeatingType> seatingType = new TableColumn<>("Seating");
        seatingType.setCellValueFactory(new PropertyValueFactory<>("aircraftSeatingType"));
        table.getColumns().add(seatingType);

        TableColumn<Aircraft, Integer> speed = new TableColumn<>("Speed");
        speed.setCellValueFactory(new PropertyValueFactory<>("speed"));
        table.getColumns().add(speed);

//        TableColumn<Aircraft, Integer> altitude = new TableColumn<>("Altitude");
//        altitude.setCellValueFactory(new PropertyValueFactory<>("altitude"));
//        table.getColumns().add(altitude);

        TableColumn<Aircraft, Integer> range = new TableColumn<>("Range");
        range.setCellValueFactory(new PropertyValueFactory<>("range"));
        table.getColumns().add(range);


        TableColumn<Aircraft, Boolean> autopilot = new TableColumn<>("Autopilot");
        autopilot.setCellValueFactory(new PropertyValueFactory<>("autopilot"));
        autopilot.setCellFactory(new CheckBoxCellFactory<>());
        table.getColumns().add(autopilot);


        TableColumn<Aircraft, AircraftSeatingType> steamGauges = new TableColumn<>("Gauges");
        steamGauges.setCellValueFactory(new PropertyValueFactory<>("gauges"));
        table.getColumns().add(steamGauges);

//        TableColumn<Aircraft, Boolean> gears = new TableColumn<>("Gears");
//        gears.setCellValueFactory(new PropertyValueFactory<>("gears"));
//        table.getColumns().add(gears);
//
//        TableColumn<Aircraft, Boolean> floats = new TableColumn<>("Floats");
//        floats.setCellValueFactory(new PropertyValueFactory<>("floats"));
//        table.getColumns().add(floats);
//
//        TableColumn<Aircraft, Boolean> skis = new TableColumn<>("Skis");
//        skis.setCellValueFactory(new PropertyValueFactory<>("skis"));
//        table.getColumns().add(skis);

        TableColumn<Aircraft, Boolean> favorite = new TableColumn<>("Favorite");
        favorite.setCellValueFactory(new PropertyValueFactory<>("favorite"));
        favorite.setCellFactory(new CheckBoxCellFactory<>());
        table.getColumns().add(favorite);

        TableColumn<Aircraft, Boolean> mastered = new TableColumn<>("Mastered");
        mastered.setCellValueFactory(new PropertyValueFactory<>("mastered"));
        mastered.setCellFactory(new CheckBoxCellFactory<>());
        table.getColumns().add(mastered);

        TableColumn<Aircraft, Boolean> toCheck = new TableColumn<>("To check");
        toCheck.setCellValueFactory(new PropertyValueFactory<>("toCheck"));
        toCheck.setCellFactory(new CheckBoxCellFactory<>());
        table.getColumns().add(toCheck);

        TableColumn<Aircraft, Integer> simAircrafts = new TableColumn<>("Sim Aircraft");
        simAircrafts.setCellValueFactory(aircraftIntegerCellDataFeatures -> new ReadOnlyObjectWrapper<>(aircraftIntegerCellDataFeatures.getValue().getSimAircraft().size()));
        table.getColumns().add(simAircrafts);

        TableColumn<Aircraft, Integer> flightCount = new TableColumn<>("Flights");
        flightCount.setCellValueFactory(aircraftIntegerCellDataFeatures -> new ReadOnlyObjectWrapper<>(FlightSimLogController.getInstance().getLogbook().getFlightCountByAircraft(aircraftIntegerCellDataFeatures.getValue())));
        table.getColumns().add(flightCount);

        TableColumn<Aircraft, String> remarks = new TableColumn<>("Tags");
        remarks.setCellValueFactory(new PropertyValueFactory<>("tags"));
        table.getColumns().add(remarks);


        aircraftListPane.setCenter(table);

        table.setRowFactory(tv -> {
            TableRow<Aircraft> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FlightSimLogController.getInstance().showAndWaitAircraft(row.getItem());

                    setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));

                } else if (event.getButton() == MouseButton.SECONDARY) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("Delete aircraft");
                    alert.setContentText("Are you sure?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        FlightSimLogController.getInstance().deleteAircraft(row.getItem());
                        setTable(logbook.getFilteredAircraftList(currentFilter, currentFilterAircraftType, currentFilterSeatingType, cbShowOutdated.isSelected()));
                    }

                }
            });
            return row;
        });

        TableViewResizer.autoSizeColumns(table);

    }


    @FXML
    void onCreateAircraft(ActionEvent event) {
        FlightSimLogController.getInstance().createAircraft();
    }

    @FXML
    void onUnlinked(ActionEvent event) {
        List<SimAircraft> unlinkedAircraft = logbook.getUnlinkedSimAircraft();
        StringBuilder sb = new StringBuilder();
        for (SimAircraft aircraft : unlinkedAircraft) {
            sb.append(aircraft.getDescription()).append(System.lineSeparator());
        }

        new TextMessageDialog((Stage) txtFilter.getScene().getWindow(), "Unlinkled sim aircraft", sb.toString()).showAndWait();



    }

    @FXML
    void onClose(ActionEvent event) {
        closeForm();
    }


    public void closeForm() {
        Stage stage = (Stage) txtFilter.getScene().getWindow();
        stage.close();

        FlightSimLogController.getInstance().showLogBook();
    }


}
