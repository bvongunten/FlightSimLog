package ch.nostromo.flightsimlog.tracker.autotracker;

import ch.nostromo.flightsimlog.tracker.TrackerListener;

public interface AutoTrackerListener extends TrackerListener {

   void onFlightStarted();

   void onFlightEnded();

}
