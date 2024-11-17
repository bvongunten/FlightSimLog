package ch.nostromo.flightsimlog.fxui.fxutils;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableViewResizer {



    /**
     * Autosizes the columns of a TableView based on the header and cell content.
     */
    public static <T> void autoSizeColumns(TableView<T> tableView) {
        for (TableColumn<T, ?> column : tableView.getColumns()) {
            column.setPrefWidth(0);

            double headerWidth = computeHeaderWidth(column);

            double cellWidth = computeMaxCellWidth(tableView, column);

            column.setPrefWidth(Math.max(headerWidth, cellWidth));
        }


    }

    /**
     * Computes the width required for the column header.
     */
    public static double computeHeaderWidth(TableColumn<?, ?> column) {
        return column.getText() != null ?
                column.getText().length() * 5.5 + 20 : 20; // Approximate character width + padding
    }

    /**
     * Computes the maximum width required for cell content in the column.
     */
    public static <T> double computeMaxCellWidth(TableView<T> tableView, TableColumn<T, ?> column) {
        double maxWidth = 0;

        for (T item : tableView.getItems()) {
            Object cellData = column.getCellData(item);
            if (cellData != null) {
                double cellWidth = cellData.toString().length() * 5.5 + 20; // Approximate width calculation
                maxWidth = Math.max(maxWidth, cellWidth);
            }
        }

        return maxWidth;
    }
}
