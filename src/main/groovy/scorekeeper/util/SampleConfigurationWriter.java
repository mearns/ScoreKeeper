package scorekeeper.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SampleConfigurationWriter {
    private static Logger log = LoggerFactory.getLogger(SampleConfigurationWriter.class);

    public static void writeSampleConfig(String whichSample, File location) {
        try {
            InputStream source = SampleConfigurationWriter.class.getResourceAsStream(whichSample);
            copyFileUsingFileStreams(source, location);
        } catch (Exception e) {
            log.error("Can't copy " + whichSample + " to " + location.getAbsolutePath(), e);
        }
    }

    private static void copyFileUsingFileStreams(InputStream input, File dest) throws IOException {
        try (OutputStream output = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        }
    }

}
