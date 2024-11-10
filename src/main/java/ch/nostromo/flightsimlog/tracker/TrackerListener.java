package ch.nostromo.flightsimlog.tracker;

public interface TrackerListener {

   void onData(TrackerData data);

   void onEventFileLoaded(String file);

   void onEventPause(int pause);

   void onEventSim(int sim);
}
