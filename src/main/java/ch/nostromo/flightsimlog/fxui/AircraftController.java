package ch.nostromo.flightsimlog.fxui;

import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.base.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AircraftController {

    @FXML
    private ChoiceBox<AircraftType> cbAircraftType;

    @FXML
    private CheckBox cbAutopilot;

    @FXML
    private CheckBox cbFavorite;

    @FXML
    private CheckBox cbFloats;

    @FXML
    private ChoiceBox<AircraftGauges> cbGauges;

    @FXML
    private CheckBox cbGears;

    @FXML
    private CheckBox cbMastered;

    @FXML
    private CheckBox cbToCheck;

    @FXML
    private ChoiceBox<AircraftPropulsion> cbPropulsion;

    @FXML
    private ChoiceBox<AircraftSeatingType> cbSeating;

    @FXML
    private CheckBox cbSkis;

    @FXML
    private BorderPane paneSimAircraft;

    @FXML
    private BorderPane paneUnlinkedAircraft;

    @FXML
    private TextField txtAltitude;

    @FXML
    private TextField txtDescription;

    @FXML
    private TextField txtEndurance;

    @FXML
    private TextField txtEngines;

    @FXML
    private TextField txtFlightCount;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtManufacturer;

    @FXML
    private TextField txtRange;

    @FXML
    private TextArea txtRemarks;

    @FXML
    private TextField txtSeats;

    @FXML
    private TextField txtSpeed;


    Aircraft aircraft;

    public void setup(Aircraft aircraft) {
        this.aircraft = aircraft;

        cbAircraftType.getItems().setAll(AircraftType.values());
        cbPropulsion.getItems().setAll(AircraftPropulsion.values());
        cbGauges.getItems().setAll(AircraftGauges.values());
        cbSeating.getItems().setAll(AircraftSeatingType.values());

        fillForm();
        fillSimAircraftTables();

    }

    private void fillForm() {

        txtManufacturer.setText(aircraft.getManufacturer());
        txtDescription.setText(aircraft.getDescription());
        cbAircraftType.getSelectionModel().select(aircraft.getAircraftType());

        txtId.setText(aircraft.getId());
        txtFlightCount.setText(String.valueOf(FlightSimLogController.getInstance().getLogbook().getFlightCountByAircraft(aircraft)));

        cbPropulsion.getSelectionModel().select(aircraft.getAircraftPropulsion());
        txtEngines.setText(String.valueOf(aircraft.getEngines()));
        cbGauges.getSelectionModel().select(aircraft.getGauges());
        cbAutopilot.setSelected(aircraft.getAutopilot());

        cbSeating.getSelectionModel().select(aircraft.getAircraftSeatingType());
        txtSeats.setText(String.valueOf(aircraft.getSeats()));
        cbGears.setSelected(aircraft.getGears());
        cbSkis.setSelected(aircraft.getSkis());
        cbFloats.setSelected(aircraft.getFloats());

        txtSpeed.setText(String.valueOf(aircraft.getSpeed()));
        txtAltitude.setText(String.valueOf(aircraft.getAltitude()));
        txtEndurance.setText(String.valueOf(aircraft.getEndurance()));
        txtRange.setText(String.valueOf(aircraft.getRange()));

        txtRemarks.setText(String.valueOf(aircraft.getRemarks()));

        cbFavorite.setSelected(aircraft.getFavorite());
        cbMastered.setSelected(aircraft.getMastered());
        cbToCheck.setSelected(aircraft.getToCheck());

    }

    private void bindAircraft() {

        aircraft.setManufacturer(txtManufacturer.getText());
        aircraft.setDescription(txtDescription.getText());
        aircraft.setAircraftType(cbAircraftType.getValue());

        aircraft.setAircraftPropulsion(cbPropulsion.getValue());
        aircraft.setEngines(Integer.parseInt(txtEngines.getText()));
        aircraft.setGauges(cbGauges.getValue());
        aircraft.setAutopilot(cbAutopilot.isSelected());

        aircraft.setAircraftSeatingType(cbSeating.getValue());
        aircraft.setSeats(Integer.parseInt(txtSeats.getText()));

        aircraft.setGears(cbGears.isSelected());
        aircraft.setSkis(cbSkis.isSelected());
        aircraft.setFloats(cbFloats.isSelected());

        aircraft.setSpeed(Integer.parseInt(txtSpeed.getText()));
        aircraft.setAltitude(Integer.parseInt(txtAltitude.getText()));
        aircraft.setEndurance(Integer.parseInt(txtEndurance.getText()));
        aircraft.setRange(Integer.parseInt(txtRange.getText()));

        aircraft.setRemarks(txtRemarks.getText());
        aircraft.setFavorite(cbFavorite.isSelected());
        aircraft.setMastered(cbMastered.isSelected());
        aircraft.setToCheck(cbToCheck.isSelected());
    }

    private void fillSimAircraftTables() {

        ObservableList<SimAircraft> simAircraftData = FXCollections.observableArrayList(aircraft.getSimAircraft());
        TableView<SimAircraft> simAircraftTable = new TableView<>();
        simAircraftTable.setItems(simAircraftData);

        TableColumn<SimAircraft, String> simAircraftDescription = new TableColumn<>("Linked Sim Aircraft");
        simAircraftDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        simAircraftTable.getColumns().add(simAircraftDescription);

        paneSimAircraft.setCenter(simAircraftTable);

        simAircraftTable.setRowFactory(tv -> {
            TableRow<SimAircraft> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    aircraft.getSimAircraft().remove(row.getItem());
                    fillSimAircraftTables();
                }
            });
            return row;
        });

        simAircraftTable.getColumns().getFirst().setPrefWidth(525);




        ObservableList<SimAircraft> unlinkedAircraftData = FXCollections.observableArrayList(FlightSimLogController.getInstance().getLogbook().getUnlinkedSimAircraft());
        TableView<SimAircraft> unLinkedAircraftTable = new TableView<>();
        unLinkedAircraftTable.setItems(unlinkedAircraftData);

        TableColumn<SimAircraft, String> unlinkedAircraftDescription = new TableColumn<>("Unlinked Sim Aircraft");
        unlinkedAircraftDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        unLinkedAircraftTable.getColumns().add(unlinkedAircraftDescription);

        paneUnlinkedAircraft.setCenter(unLinkedAircraftTable);

        unLinkedAircraftTable.setRowFactory(tv -> {
            TableRow<SimAircraft> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    aircraft.getSimAircraft().add(row.getItem());
                    this.fillSimAircraftTables();
                }
            });
            return row;
        });

        unLinkedAircraftTable.getColumns().getFirst().setPrefWidth(525);

    }


    @FXML
    void onClose(ActionEvent event) {
        closeForm();
    }


    public void closeForm() {
        bindAircraft();

        Stage stage = (Stage) txtId.getScene().getWindow();
        stage.close();


        FlightSimLogController.getInstance().saveLogbookToFile();

        FlightSimLogController.getInstance().showAircraftList();
    }
}