package ch.nostromo.flightsimlog.utils;


import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.flight.Flight;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ScreenshotGrabber extends Thread {


    String sourcedirectory = "C:\\Users\\bvg\\appdata\\Roaming\\Microsoft Flight Simulator 2024\\Screenshot";


    int currentFilesCount = 0;

    boolean running = false;

    String prefix;

    File sourceDir;
    File targetDir;

    List<File> knownFiles = new ArrayList<>();

    public ScreenshotGrabber(Flight flight) {
        this.prefix = flight.getId();
        this.targetDir = flight.getFlightImagesPath();
        this.currentFilesCount = flight.getImagesCount();


        sourceDir = new File(sourcedirectory);

        if (sourceDir.exists() && sourceDir.isDirectory()) {
            knownFiles = Arrays.stream(sourceDir.listFiles())
                    .filter(File::isFile)
                    .collect(Collectors.toList());
        }


    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @SneakyThrows
    public void run() {
        running = true;

        while (running) {

            try {
                List<File> currentFiles = Arrays.stream(sourceDir.listFiles())
                        .filter(File::isFile)
                        .collect(Collectors.toList());

                for (File currentFile : currentFiles) {
                    if (!knownFiles.contains(currentFile)) {

                        if (!targetDir.exists()) {
                            targetDir.mkdirs();
                        }

                        File targetFile = new File(targetDir, currentFile.getName());
                        currentFile.renameTo(targetFile);
                        Toolkit.getDefaultToolkit().beep();

                    }

                }

                knownFiles = currentFiles;

            } catch (Exception e) {
                throw new FlightSimLogException("Unable to copy image with message: " + e.getMessage(), e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                running = false;
            }
        }


    }

}
