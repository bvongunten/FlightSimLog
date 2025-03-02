package ch.nostromo.flightsimlog.fxui.dialogs;

import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.data.base.Category;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriesDialog extends Dialog<Void> {

    private final List<Category> items;
    private final ListView<Category> listView;

    public CategoriesDialog(List<Category> items) {
        this.items = new ArrayList<>(items); // Make a copy of the list to work with

        this.initModality(Modality.APPLICATION_MODAL); // Make it a modal dialog
        this.setTitle("Edit Categories");

        listView = new ListView<>();
        listView.getItems().addAll(this.items);

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnEdit = new Button("Edit");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = new Button("Delete");
        btnDelete.setOnAction(e -> deleteSelectedItem());

        VBox layout = new VBox(10, listView, btnAdd, btnEdit, btnDelete);
        layout.setPadding(new Insets(10));

        this.getDialogPane().setContent(layout);
        this.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Handle close button to update original list
        this.setOnCloseRequest(e -> {
            items.clear();
            items.addAll(listView.getItems());
        });
    }

    private void showAddDialog() {
        Dialog<Category> dialog = createObjectDialog("Add New Item", null);
        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(item -> listView.getItems().add(item));
    }

    private void showEditDialog() {
        Category selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Dialog<Category> dialog = createObjectDialog("Edit Item", selectedItem);
            Optional<Category> result = dialog.showAndWait();
            result.ifPresent(editedItem -> {
                int index = listView.getItems().indexOf(selectedItem);
                listView.getItems().set(index, editedItem);
            });
        } else {
            showAlert("Please select an item to edit.");
        }
    }

    private void deleteSelectedItem() {
        Category selectedItem = listView.getSelectionModel().getSelectedItem();

        if (selectedItem.getId().equals("C-1")) {
            showAlert("C-1 not deletable");
            return;
        } else if (FlightSimLogController.getInstance().getLogbook().isCategoryInUse(selectedItem)) {

            showAlert("Category " + selectedItem.getDescription() + " is still referenced");
            return;
        }

        if (selectedItem != null) {
            listView.getItems().remove(selectedItem);
        } else {
            showAlert("Please select an item to delete.");
        }
    }

    private Dialog<Category> createObjectDialog(String title, Category item) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField(item == null ? String.valueOf(FlightSimLogController.getInstance().getLogbook().getNextCategoryId()) : item.getId());
        idField.setEditable(false); // Disable editing of ID field
        TextField descriptionField = new TextField(item == null ? "" : item.getDescription());
        descriptionField.setPromptText("Description");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (item == null) {
                    return new Category(idField.getText(), descriptionField.getText(), true);
                } else {
                    return new Category(item.getId(), descriptionField.getText(), true); // Preserve the existing ID
                }
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }
}