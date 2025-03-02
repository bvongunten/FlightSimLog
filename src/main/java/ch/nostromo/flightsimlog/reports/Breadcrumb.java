package ch.nostromo.flightsimlog.reports;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Breadcrumb {
    String title;
    String link;

}
