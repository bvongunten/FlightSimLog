package ch.nostromo.flightsimlog.tracker;

public interface TrackerListener {

   void onData(TrackerData data);

   void onEventFileLoaded(String file);

   void onEventPause(int pause);

   void onEventSimStart(int start);

   void onEventSimStop(int stop);

   void onEventSim(int sim);

   void onConnected();

   void onDisconnected();

   void onException(Throwable e);


}
