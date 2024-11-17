package ch.nostromo.flightsimlog.fxui.fxutils;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class CheckBoxCellFactory<T> implements Callback<TableColumn<T, Boolean>, TableCell<T, Boolean>> {
    @Override
    public TableCell<T, Boolean> call(TableColumn<T, Boolean> param) {
        return new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            private final HBox hBox = new HBox(checkBox);
            {
                hBox.setAlignment(Pos.CENTER); // Center align the checkbox
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                checkBox.setSelected(item);
                checkBox.setDisable(true);
                setGraphic(hBox);
            }
        };
    }
}