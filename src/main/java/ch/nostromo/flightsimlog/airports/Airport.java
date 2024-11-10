package ch.nostromo.flightsimlog.airports;

import ch.nostromo.flightsimlog.data.coordinates.Coordinates;
import lombok.Data;

@Data
public class Airport {

    String icao;
    String name;
    Coordinates coordinates = new Coordinates();


}
