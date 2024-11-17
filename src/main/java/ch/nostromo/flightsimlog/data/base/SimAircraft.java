package ch.nostromo.flightsimlog.data.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@XmlRootElement
@EqualsAndHashCode(callSuper=false)
public class SimAircraft {

    @XmlID
    String id;

    @XmlElement
    String description;

    @Override
    public String toString() {
        return description;
    }


}
