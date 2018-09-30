package org.openstreetmap.josm.plugins.ods.arcgis.rest.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.JsonUtil;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.AGRestFeatureLayer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureLayerParseTest {

    @Test
    public void testParseFeatureLayer() throws IOException {
        String json = getJson();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AGRestFeatureLayer layer = mapper.readValue(json, AGRestFeatureLayer.class);
        layer.toString();
    }

    private String getJson() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/featureLayerTest.json")) {
            return JsonUtil.readUTF8InputStream(is);
        }
    }

}
