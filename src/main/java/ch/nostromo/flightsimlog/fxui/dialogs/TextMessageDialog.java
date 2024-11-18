package ch.nostromo.flightsimlog.fxui.dialogs;

import ch.nostromo.flightsimlog.utils.ClipboardTools;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TextMessageDialog extends Stage {

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    public TextMessageDialog(Stage owner, String title, String text) {
        setTitle(title);
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);

        TextArea textArea = new TextArea(text);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        VBox.setVgrow(textArea, Priority.ALWAYS);

        Button copyButton = new Button("Copy to Clipboard");
        Button okButton = new Button("OK");

        copyButton.setOnAction(event -> ClipboardTools.pasteToClipboard(textArea.getText()));
        okButton.setOnAction(event -> close());

        HBox buttonBox = new HBox(10, copyButton, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));

        VBox mainLayout = new VBox(textArea, buttonBox);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setPrefSize(WIDTH, HEIGHT);

        Scene scene = new Scene(mainLayout, WIDTH, HEIGHT);
        setScene(scene);

        setResizable(true);
    }
}