package ch.nostromo.flightsimlog.reports;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Breadcrumbs {

    List<Breadcrumb> breadcrumbs = new ArrayList<>();

    public String getNavigation() {
        String result = "<h5>";
        for (int i = 0; i < breadcrumbs.size(); i++) {
            if (i > 0) {
                result += " > ";
            }

            result += "<a href=\"" + breadcrumbs.get(i).getLink() + "\">" + breadcrumbs.get(i).getTitle() + "</a>";


        }
        result += "</h5>";
        return result;

    }


    public Breadcrumbs clone() {
        Breadcrumbs clone = new Breadcrumbs();

        clone.breadcrumbs = new ArrayList<>(breadcrumbs);
        return clone;
    }

}
