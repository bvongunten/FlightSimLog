package ch.nostromo.flightsimlog;

import javafx.application.Application;
import javafx.stage.Stage;

public class FlightSimLog extends Application {


    /*
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage)  {
        FlightSimLogController.createInstance(primaryStage);

        FlightSimLogController.getInstance().showLogBook();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            FlightSimLogController.getInstance().showError(throwable);
        });


    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
