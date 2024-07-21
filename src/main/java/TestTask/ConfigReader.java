package TestTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(Files.newInputStream(Paths.get("src/test/resources/config.properties")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


}

