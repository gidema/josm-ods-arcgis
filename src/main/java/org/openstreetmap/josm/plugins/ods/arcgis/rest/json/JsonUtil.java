package org.openstreetmap.josm.plugins.ods.arcgis.rest.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {
    public static void checkError(JsonNode node) throws IOException {
        JsonNode errorNode = node.get("error");
        if (errorNode == null) {
            return;
        }
        String errorMessage = errorNode.get("message").asText();
        throw new IOException(errorMessage);
    }

    public static String readFile(File file) throws FileNotFoundException, IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readUTF8InputStream(fis);
        }
    }

    public static String readUTF8InputStream(InputStream is) throws IOException {
        Reader reader = new InputStreamReader(is, "UTF-8");
        return readToString(reader);
    }

    public static String readToString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
