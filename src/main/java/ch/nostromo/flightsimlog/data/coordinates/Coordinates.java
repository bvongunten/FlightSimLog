package ch.nostromo.flightsimlog.data.coordinates;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class Coordinates {

    @XmlAttribute
    double coordLat;
    @XmlAttribute
    double coordLon;
    @XmlAttribute
    double coordAlt;

    public Coordinates(double coordLat, double coordLon, double coordAlt) {
        this.coordLat = coordLat;
        this.coordLon = coordLon;
        this.coordAlt = coordAlt;
    }


}
