package ch.nostromo.flightsimlog.fxui.fxutils;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DynamicTableViewBuilder builds a {@Link TableView} instance using reflection based on a given type and a
 * list of field names.
 *
 * @author Bernhard von Gunten <bvg@nostromo.ch>
 */
public class DynamicTableViewBuilder {

    private DynamicTableViewBuilder() {
        // Do nothing ;)
    }

    /**
     * Builds the upon.
     *
     * @param <T>
     *            the generic type
     * @param model
     *            the model
     * @param fields
     *            the fields
     * @return the table view
     */
    public static <T> TableView<T> buildUpon(List<String> fields) {
        TableView<T> table = new TableView<>();
        ArrayList<TableColumn<T, ?>> columns = new ArrayList<>();

        for (String fieldName : fields) {

            TableColumn<T, ?> column = new TableColumn<>(capitalizeFirst(fieldName));
            column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
            columns.add(column);

        }

        table.getColumns().addAll(columns);

        return table;
    }

    private static String capitalizeFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
