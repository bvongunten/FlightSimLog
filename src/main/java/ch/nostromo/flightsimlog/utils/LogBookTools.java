package ch.nostromo.flightsimlog.utils;

import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.Logbook;
import ch.nostromo.flightsimlog.data.base.Aircraft;
import ch.nostromo.flightsimlog.data.base.Category;
import ch.nostromo.flightsimlog.data.flight.Flight;
import ch.nostromo.flightsimlog.data.flight.SimulationData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class LogBookTools {

    public static void saveXml(Logbook logbook) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Logbook.class, Flight.class, Aircraft.class, Category.class);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(logbook, logbook.getLogbookFile());
        } catch (JAXBException e) {
            throw new FlightSimLogException(e);
        }
    }

    public static Logbook loadXml(File file) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Logbook.class, Flight.class, Aircraft.class, Category.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            Logbook result = (Logbook) unmarshaller.unmarshal(file);

            // Transient file paths
            result.setLogbookFile(file);
            for (Flight flight : result.getFlights()) {
                flight.setLogbookFile(file);
            }


            return result;

        } catch (JAXBException e) {
            throw new FlightSimLogException(e);
        }

    }


    public static void saveSimulationData(SimulationData externalSimulationData, String fileName)  {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SimulationData.class);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(externalSimulationData, new File(fileName));
        } catch (JAXBException e) {
            throw new FlightSimLogException(e);
        }

    }

    public static SimulationData loadOrCreateSimulationData(String fileName)  {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SimulationData.class);
            File xmlFile = new File(fileName);

            if (xmlFile.exists()) {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return (SimulationData) unmarshaller.unmarshal(xmlFile);
            } else {
                return new SimulationData();
            }
        } catch (JAXBException e) {
            throw new FlightSimLogException(e);
        }

    }


}
