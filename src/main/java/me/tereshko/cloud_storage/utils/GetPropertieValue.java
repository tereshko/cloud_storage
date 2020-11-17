package me.tereshko.cloud_storage.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertieValue {
    public String getSQLLITE_URL() {
        String url = readPropertieFromFile("SQLLITE_URL");
        return url;
    }

    public int getPORT() {
        int port = Integer.parseInt(readPropertieFromFile("PORT"));
        return port;
    }

    private String readPropertieFromFile(String propertyName) {
        String result = null;
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            result = prop.getProperty(propertyName);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

}
