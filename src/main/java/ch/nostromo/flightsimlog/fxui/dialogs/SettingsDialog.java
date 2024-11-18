package ch.nostromo.flightsimlog.fxui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class SettingsDialog extends Dialog<Pair<String, String>> {

    public SettingsDialog(String initialHost, String initialPort) {
        this.setTitle("Configuration");
        this.setHeaderText("Enter Host and Port");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField hostField = new TextField();
        hostField.setPromptText("Host");
        hostField.setText(initialHost);

        TextField portField = new TextField();
        portField.setPromptText("Port");
        portField.setText(initialPort);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Host:"), 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);

        this.getDialogPane().setContent(grid);

        Node okButton = this.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        hostField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || portField.getText().trim().isEmpty());
        });
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || hostField.getText().trim().isEmpty());
        });

        this.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(hostField.getText(), portField.getText());
            }
            return null;
        });
    }
}