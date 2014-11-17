package scorekeeper.util;

import java.io.*;

public class SampleConfigurationWriter {

    public static void writeSampleConfigAndDie() {
        try {
            InputStream source = SampleConfigurationWriter.class.getResourceAsStream("sample.system-props.conf");
            copyFileUsingFileStreams(source, new File("config/system-props.conf"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);
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
