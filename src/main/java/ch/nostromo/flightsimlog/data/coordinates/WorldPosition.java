package ch.nostromo.flightsimlog.data.coordinates;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class WorldPosition extends Coordinates {

    @XmlElement
    String icao = "";

    @XmlElement
    String description = "";

    public WorldPosition(double coordLat, double coordLong, double alt, String icao, String description) {
        super(coordLat, coordLong, alt);
        this.icao = icao;
        this.description = description;
    }

}
