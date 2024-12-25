package ch.nostromo.flightsimlog.reports.html;

import ch.nostromo.flightsimlog.utils.OutputFormatter;
import lombok.Data;

import java.util.Calendar;

@Data
public class HtmlTableField {

    Object content;
    HtmlTableColumn column;
    String link;

    public HtmlTableField(Object content, HtmlTableColumn column, String link) {
        this.content = content;
        this.column = column;
        this.link = link;
    }

    public HtmlTableField(Object content, HtmlTableColumn column) {
        this(content, column, null);
    }

    public String createHtml() {
        if (link == null) {
            return getContentString();
        } else {
            return "<a href=\"" + link + "\">" + getContentString() + "</a>";
        }

    }

    private String getContentString() {
        switch (getColumn().columnType) {
            case CALENDAR -> {
                Calendar cal = (Calendar) getContent();
                return OutputFormatter.createTime(cal);
            }
            case DISTANCE -> {
                Double distance = (Double) getContent();
                return OutputFormatter.createDistance(distance);
            }
            case TIME -> {
                Integer seconds = (Integer) getContent();
                return OutputFormatter.createDuration(seconds);
            }
            default -> {
                return getContent().toString();
            }

        }


    }

}
