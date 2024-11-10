package ch.nostromo.flightsimlog;

import java.util.prefs.Preferences;

/**
 * Config, under windows: Computer\HKEY_CURRENT_USER\Software\JavaSoft\Prefs\ch\nostromo\flightsimlog
 */
public class FlightSimLogConfig {

    private static final String SIM_CONNECT_HOST = "simconnecthost";
    private static final String SIM_CONNECT_PORT = "simconnectport";
    private static final String LOGBOOK_FILE = "logbookfile";


    private static Preferences preferences() {
        return Preferences.userNodeForPackage(FlightSimLogConfig.class);
    }

    public static String getSimConnectHost() {
        return preferences().get(SIM_CONNECT_HOST, "127.0.0.1");
    }

    public static void putSimConnectHost(String host) {
        preferences().put(SIM_CONNECT_HOST, host);
    }

    public static Integer getSimConnectPort() {
        return preferences().getInt(SIM_CONNECT_PORT, 500);
    }

    public static void putSimConnectPort(int port) {
        preferences().putInt(SIM_CONNECT_PORT, port);
    }


    public static String getLogbookFile() {
        return preferences().get(LOGBOOK_FILE, null);
    }

    public static void putLogbookFile(String file) {
        preferences().put(LOGBOOK_FILE, file);
    }


}
