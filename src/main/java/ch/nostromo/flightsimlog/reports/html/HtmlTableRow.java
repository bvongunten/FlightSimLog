package ch.nostromo.flightsimlog.reports.html;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HtmlTableRow {

    List<HtmlTableField> columns = new ArrayList<>();

    public void addField(Object field, HtmlTableColumn column, String link) {
        columns.add(new HtmlTableField(field, column, link));
    }

    public void addField(Object field, HtmlTableColumn column) {
        columns.add(new HtmlTableField(field, column));
    }

}
