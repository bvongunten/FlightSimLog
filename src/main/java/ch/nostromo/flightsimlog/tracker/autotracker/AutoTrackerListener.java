package ch.nostromo.flightsimlog.tracker.autotracker;

import ch.nostromo.flightsimlog.tracker.TrackerData;

public interface AutoTrackerListener {

   void onFlightStarted();

   void onFlightEnded();

}
