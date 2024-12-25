package ch.nostromo.flightsimlog.reports.html;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class HtmlTableColumn {


    public enum HtmlColumnType {
        STRING,
        CALENDAR,
        DISTANCE,
        TIME
    }

    String title;
    HtmlColumnType columnType;



}
