package ch.nostromo.flightsimlog;

public class FlightSimLogException extends RuntimeException{

    public FlightSimLogException(Exception e) {
        super(e);
    }

    public FlightSimLogException(String message) {
        super(message);
    }

    public FlightSimLogException(String message, Exception e) {
        super(message, e);
    }


}
