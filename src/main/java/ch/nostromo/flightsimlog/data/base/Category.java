package ch.nostromo.flightsimlog.data.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class Category {

    @XmlID
    String id;

    @XmlElement
    String description;

    @XmlElement
    Boolean generateReport = true;

    @XmlElement
    Boolean generateDetailedRoute = true;

    @Override
    public String toString() {
        return description;
    }

}
