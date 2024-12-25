package ch.nostromo.flightsimlog.reports.html;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HtmlTable {

    List<HtmlTableRow> rows = new ArrayList<>();

    public String getHtmlTable() {
        StringBuilder html = new StringBuilder();

        if (!rows.isEmpty()) {

            // table
            html.append("<table class=\"highlight striped responsive-table\">");

            // header
            html.append("<thead><tr>");
            for (HtmlTableField field : rows.getFirst().getColumns()) {
                html.append("<th class=\"sortable\" data-sort=\"" + field.getColumn().getTitle() + "\">");
                html.append(field.getColumn().getTitle());
                html.append("</th>");
            }
            html.append("</tr></thead>");


            // Rows
            html.append("<tbody>");

            for (HtmlTableRow row : rows) {
                html.append("<tr>");

                for (HtmlTableField field : row.getColumns()) {
                    html.append("<td>");
                    html.append(field.createHtml());
                    html.append("</td>");
                }

                html.append("</tr>");
            }

            html.append("</tbody>");
            html.append("</table>");


        }
        return html.toString();

    }

}
