package org.openstreetmap.josm.plugins.ods.arcgis.rest.json;

import java.io.IOException;

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
}
