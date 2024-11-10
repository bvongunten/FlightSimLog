module ch.nostromo.flightsimlog {
    requires java.logging;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires geojson;


    requires java.desktop;

    requires transitive java.xml.bind;
    requires transitive com.sun.xml.bind;
    requires static lombok;
    requires pngtastic;
    requires java.net.http;
    requires org.lembeck.fs.simconnect;
    requires javafx.media;

    exports ch.nostromo.flightsimlog;
    exports ch.nostromo.flightsimlog.data;
    exports ch.nostromo.flightsimlog.data.base;
    exports ch.nostromo.flightsimlog.data.coordinates;
    exports ch.nostromo.flightsimlog.data.flight;
    exports ch.nostromo.flightsimlog.fxui;
    exports ch.nostromo.flightsimlog.fxui.fxutils;

    opens ch.nostromo.flightsimlog;
    opens ch.nostromo.flightsimlog.data;
    opens ch.nostromo.flightsimlog.data.base;
    opens ch.nostromo.flightsimlog.data.coordinates;
    opens ch.nostromo.flightsimlog.data.flight;

    opens ch.nostromo.flightsimlog.fxui;
    opens ch.nostromo.flightsimlog.fxui.fxutils;
}