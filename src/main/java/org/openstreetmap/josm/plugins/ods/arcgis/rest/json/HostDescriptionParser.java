package org.openstreetmap.josm.plugins.ods.arcgis.rest.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.plugins.ods.arcgis.rest.AGRestHost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Deprecated
public class HostDescriptionParser {
    public static void parseHostJson(InputStream inputStream, AGRestHost host)
            throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(inputStream);
        JsonUtil.checkError(node);

        @SuppressWarnings("unused")
        String serviceDescription = node.get("serviceDescription").textValue();
        @SuppressWarnings("unused")
        String description = node.get("description").textValue();
        @SuppressWarnings("unused")
        String copyrightText = node.get("copyrightText").textValue();
        Iterator<JsonNode> it = node.get("layers").elements();
        List<String> featureTypes = new LinkedList<>();
        while (it.hasNext()) {
            JsonNode featureNode = it.next();
            Long id = featureNode.get("id").asLong();
            String name = featureNode.get("name").textValue();
            Long parentLayerId = featureNode.get("parentLayerId").asLong();
            if (parentLayerId != -1) {
                String feature = name + "/" + id;
                featureTypes.add(feature);
            }
        }
        //        host.setFeatureTypes(featureTypes);
    }
}
