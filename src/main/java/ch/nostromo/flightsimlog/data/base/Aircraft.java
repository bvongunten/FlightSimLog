package ch.nostromo.flightsimlog.data.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class Aircraft {


    @XmlID
    String id;

    @XmlElement
    String description = "";

    @XmlElement
    String manufacturer = "";

    @XmlElement
    AircraftType aircraftType = AircraftType.EXPERIMENTAL_DIVERS;

    @XmlElement
    AircraftPropulsion aircraftPropulsion = AircraftPropulsion.OTHER;

    @XmlElement
    Integer engines = 0;

    @XmlElement
    AircraftSeatingType aircraftSeatingType = AircraftSeatingType.OTHER;

    @XmlElement
    Integer speed = 0;

    @XmlElement
    Integer altitude = 0;

    @XmlElement
    Integer range = 0;

    @XmlElement
    Boolean autopilot = Boolean.FALSE;

    @XmlElement
    AircraftGauges gauges = AircraftGauges.STEAM;

    @XmlElement
    Boolean gear = Boolean.TRUE;

    @XmlElement
    Boolean floats = Boolean.FALSE;

    @XmlElement
    Boolean skis = Boolean.FALSE;

    @XmlElement
    Boolean favorite = Boolean.FALSE;

    @XmlElement
    Boolean mastered = Boolean.FALSE;

    @XmlElement
    Boolean toCheck = Boolean.TRUE;

    @XmlElement
    Boolean outdated = Boolean.FALSE;

    @XmlElement
    String tags = "";

    @XmlElement
    String remarks = "";

    @XmlElementWrapper(name = "simAircraft")
    @XmlIDREF
    List<SimAircraft> simAircraft = new ArrayList<>();


    @Override
    public String toString() {
        return description;
    }


}
