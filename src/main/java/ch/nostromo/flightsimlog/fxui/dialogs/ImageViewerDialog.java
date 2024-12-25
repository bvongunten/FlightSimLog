package ch.nostromo.flightsimlog.fxui.dialogs;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerDialog {

    private final List<File> imageFiles;
    private int currentIndex = 0;
    private final Stage dialogStage;
    private final ImageView imageView;

    public ImageViewerDialog(Stage owner, File directory) {
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory!");
        }

        // Load PNG files from the directory
        imageFiles = new ArrayList<>();
        File[] files = directory.listFiles(file -> file.isFile() && file.getName().toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File file : files) {
                imageFiles.add(file);
            }
        }

        if (imageFiles.isEmpty()) {
            throw new IllegalArgumentException("No PNG images found in the directory!");
        }

        // Initialize the dialog stage
        dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Image Viewer");
        dialogStage.setResizable(true); // Make the dialog resizable
        dialogStage.setWidth(1200);    // Set initial width
        dialogStage.setHeight(800);   // Set initial height

        // ImageView to display the current image
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(1100); // Adjust to fit within initial size
        imageView.setFitHeight(700);

        // Show the first image
        loadImage();

        // Navigation Buttons
        Button btnBack = new Button("Back");
        Button btnNext = new Button("Next");
        Button btnDelete = new Button("Delete");
        Button btnClose = new Button("Close");

        btnBack.setOnAction(e -> showPreviousImage());
        btnNext.setOnAction(e -> showNextImage());
        btnDelete.setOnAction(e -> deleteCurrentImage());
        btnClose.setOnAction(e -> dialogStage.close());

        HBox controls = new HBox(10, btnBack, btnNext, btnDelete, btnClose);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Layout
        BorderPane root = new BorderPane();
        root.setCenter(imageView);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1200, 800); // Start with 1200x800
        dialogStage.setScene(scene);

        // Bind the image size to the stage size for dynamic resizing
        dialogStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            imageView.setFitWidth(newVal.doubleValue() - 100);
        });
        dialogStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            imageView.setFitHeight(newVal.doubleValue() - 100);
        });
    }

    public void show() {
        dialogStage.showAndWait();
    }

    private void loadImage() {
        if (imageFiles.isEmpty()) {
            imageView.setImage(null);
            return;
        }

        File currentFile = imageFiles.get(currentIndex);
        Image image = new Image(currentFile.toURI().toString());
        imageView.setImage(image);
    }

    private void showPreviousImage() {
        if (imageFiles.isEmpty()) return;

        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
        loadImage();
    }

    private void showNextImage() {
        if (imageFiles.isEmpty()) return;

        currentIndex = (currentIndex + 1) % imageFiles.size();
        loadImage();
    }

    private void deleteCurrentImage() {
        if (imageFiles.isEmpty()) return;

        File currentFile = imageFiles.get(currentIndex);
        boolean deleted = currentFile.delete();

        if (deleted) {
            imageFiles.remove(currentIndex);

            // Adjust the index and load the next image
            if (imageFiles.isEmpty()) {
                imageView.setImage(null);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "All images deleted!", ButtonType.OK);
                alert.showAndWait();
            } else {
                currentIndex = currentIndex % imageFiles.size();
                loadImage();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete the image!", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
