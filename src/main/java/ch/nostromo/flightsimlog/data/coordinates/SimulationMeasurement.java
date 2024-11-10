package ch.nostromo.flightsimlog.data.coordinates;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class SimulationMeasurement extends Coordinates {

    @XmlAttribute
    boolean onGround;
    @XmlAttribute
    long time;

    public SimulationMeasurement(double coordLat, double coordLong, double height, boolean onGround, long time) {
        super(coordLat, coordLong, height);
        this.onGround = onGround;
        this.time = time;
    }

    public SimulationMeasurement createCopy() {
        return new SimulationMeasurement(this.coordLat, this.coordLon, this.coordAlt, this.onGround, this.time);
    }

}
