package ch.nostromo.flightsimlog.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FileTools {

    public static File[] getPngsInDirectory(File directory) {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().endsWith(".PNG");
            }
        });

        return files;
    }

    public static File[] getPngsInDirectory(File directory, String prefix) {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toUpperCase().startsWith(prefix.toUpperCase()) && name.toUpperCase().endsWith(".PNG");
            }
        });

        return files;
    }

}
