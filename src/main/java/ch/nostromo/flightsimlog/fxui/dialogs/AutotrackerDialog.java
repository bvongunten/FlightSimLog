package ch.nostromo.flightsimlog.fxui.dialogs;

import ch.nostromo.flightsimlog.FlightSimLogController;
import ch.nostromo.flightsimlog.tracker.TrackerData;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTracker;
import ch.nostromo.flightsimlog.tracker.autotracker.AutoTrackerListener;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AutotrackerDialog extends Stage implements AutoTrackerListener {

    static final int SLEEP = 5000;

    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    AutoTracker autotracker;

    TextArea textArea;
    Button toggleButton;

    boolean running = false;

    boolean charWriter = false;

    long lastStart = 0L;

    public AutotrackerDialog(Stage owner) {
        setTitle("Auto Tracker");
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);

        textArea = new TextArea("");
        textArea.setEditable(false);
        textArea.setWrapText(true);

        VBox.setVgrow(textArea, Priority.ALWAYS);

        toggleButton = new Button("Start Tracker");
        Button okButton = new Button("Close");

        toggleButton.setOnAction(event -> toggleMode());
        okButton.setOnAction(event -> closeDialog());

        HBox buttonBox = new HBox(10, toggleButton, okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));

        VBox mainLayout = new VBox(textArea, buttonBox);
        mainLayout.setSpacing(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setPrefSize(WIDTH, HEIGHT);

        Scene scene = new Scene(mainLayout, WIDTH, HEIGHT);
        setScene(scene);

        setResizable(true);


        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            onException(e);
        });

        toggleMode();
    }

    public void toggleMode() {

        if (!running) {
            running = true;
            autotracker = new AutoTracker(this);
            startTimedTracker();
            toggleButton.setText("Stop Tracker");
        } else {
            running = false;
            autotracker.stopTracker();
            toggleButton.setText("Start Tracker");
        }

    }

    public void closeDialog() {
        if (autotracker != null) {
            try {
                autotracker.stopTracker();
            } catch (Exception e) {
                // ignored
            }
        }

        // Back to normal exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            FlightSimLogController.getInstance().showError(throwable);
        });

        close();

    }

    private void startTimedTracker() {

        if (lastStart + SLEEP > System.currentTimeMillis()) {
            addLine("Waiting for next connect ...");
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        lastStart = System.currentTimeMillis();

        if (running) {

            addLine("Connecting ...");

            Service<Void> service = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            autotracker.startTracker();
                            return null;
                        }
                    };
                }
            };

            service.setOnFailed(event -> {
                Throwable exception = service.getException();
                onException(exception);
            });



            service.start();


        }
    }

    private void addLine(String line) {
        Platform.runLater(() -> {
            if (charWriter) {
                this.textArea.appendText(System.lineSeparator() + line + System.lineSeparator());
            } else {
                this.textArea.appendText(line + System.lineSeparator());
            }

            charWriter = false;
        });

    }

    private void addChar(String chars) {
        Platform.runLater(() -> {
            this.textArea.appendText(chars );

            charWriter = true;
        });

    }


    @Override
    public void onFlightStarted() {
        addLine(">>>>> Flight started ...");
    }

    @Override
    public void onFlightEnded(String flightDescr) {
        addLine("<<<<<< Flight ended: " + flightDescr);
    }

    @Override
    public void onData(TrackerData data) {
        addChar(".");
    }

    @Override
    public void onEventFileLoaded(String file) {
        addLine("File loaded: " + file);
    }

    @Override
    public void onEventPause(int pause) {
        addChar("P" + pause);
    }

    @Override
    public void onEventSim(int sim) {
        addChar("Sim: " + sim);
    }

    @Override
    public void onConnected() {
        addLine("SimConnect connected");
    }

    @Override
    public void onDisconnected() {
        addLine("SimConnect disconnected");
    }

    @Override
    public void onException(Throwable e) {
        addLine("###### Exception occured: " + e.getMessage());

        this.lastStart = System.currentTimeMillis();

        startTimedTracker();
    }
}