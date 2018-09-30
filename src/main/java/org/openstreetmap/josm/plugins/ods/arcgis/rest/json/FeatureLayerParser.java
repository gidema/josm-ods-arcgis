package org.openstreetmap.josm.plugins.ods.arcgis.rest.json;

import java.io.IOException;

import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.AGRestFeatureLayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureLayerParser {

    public static AGRestFeatureLayer parse(String json)
            throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, AGRestFeatureLayer.class);
    }
}
