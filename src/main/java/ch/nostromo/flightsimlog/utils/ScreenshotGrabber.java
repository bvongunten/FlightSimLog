package ch.nostromo.flightsimlog.utils;


import ch.nostromo.flightsimlog.FlightSimLogException;
import ch.nostromo.flightsimlog.data.flight.Flight;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

@Getter
@Setter
public class ScreenshotGrabber extends Thread {


    int currentFilesCount = 0;

    boolean running = false;

    String prefix;
    File dir;

    public ScreenshotGrabber(Flight flight) {
        this.prefix = flight.getId();
        this.dir = flight.getFlightImagesPath();
        this.currentFilesCount = flight.getImagesCount();

        // Flush clipboard
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        } catch (Exception ignore) {
            // Ignore
        }

    }

    @SneakyThrows
    public void run() {
        running = true;

        while (running) {

            try {
                Transferable content = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

                if (content != null && content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    currentFilesCount++;

                    BufferedImage img = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
                    String fileName = this.prefix + "-" + System.currentTimeMillis() + ".png";

                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File outfile = new File(dir, fileName);
                    ImageIO.write(img, "png", outfile);

                    StringSelection stringSelection = new StringSelection("Screenshot grabbed and saved to " + outfile.getAbsolutePath());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);

                    Toolkit.getDefaultToolkit().beep();

                }
            } catch (Exception e) {
                throw new FlightSimLogException("Unable to grab image with message: " + e.getMessage(), e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                running = false;
            }
        }


    }

}
