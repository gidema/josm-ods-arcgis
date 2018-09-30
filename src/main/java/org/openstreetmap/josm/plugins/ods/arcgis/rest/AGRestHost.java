package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.HostConfig;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.FeatureLayerParser;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.JsonUtil;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.AGRestFeatureLayer;
import org.openstreetmap.josm.plugins.ods.util.MessageBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @Host implementation for ArcGist REST services.
 *
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class AGRestHost {
    private final HostConfig config;
    private URL serviceUrl;
    private String errorMessage = "";

    private final Map<Long, AGRestFeatureSource> featureSourceCache = new HashMap<>();

    public AGRestHost(HostConfig hostConfig) {
        this.config = hostConfig;
    }

    public String getName() {
        return config.getServiceName();
    }

    public URL getServiceUrl() {
        return serviceUrl;
    }

    public boolean initialize() {
        MessageBuilder mb = new MessageBuilder();
        try {
            this.serviceUrl = new URL(config.getServiceUrl());
            if (!serviceIsAvailable()) {
                mb.appendI18n(
                        "The service at {0} is not available", serviceUrl);
            }
        } catch (MalformedURLException e) {
            mb.appendI18n(
                    "The url for the Arcgis rest service is not valid: {0}",
                    config.getServiceUrl());
        }
        this.errorMessage = mb.toString();
        return isAvailable();
    }

    public boolean isAvailable() {
        return errorMessage.length() == 0;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public AGRestFeatureSource getFeatureSource(Long featureId)
            throws JsonProcessingException, IOException {
        AGRestFeatureSource featureSource = featureSourceCache.get(featureId);
        if (featureSource == null) {
            AGRestFeatureLayer featureLayer = loadFeatureLayer(featureId);
            featureSource = new AGRestFeatureSource(this, featureLayer);
            featureSourceCache.put(featureId, featureSource);
        }
        return featureSource;
    }

    private AGRestFeatureLayer loadFeatureLayer(Long featureId)
            throws IOException {
        String json = loadFeatureLayerJson(featureId);
        return FeatureLayerParser.parse(json);
    }

    private String loadFeatureLayerJson(Long featureId) throws IOException {
        try (HttpRequest request = new HttpRequest();) {
            request.open("GET", this.serviceUrl + "/" + featureId.toString());
            request.addParameter("f", "json");
            HttpResponse response = request.send();
            return JsonUtil.readUTF8InputStream(response.getInputStream());
        }
    }

    private boolean serviceIsAvailable() {
        try (HttpRequest request = new HttpRequest();) {
            request.open("GET", serviceUrl.toString());
            request.addParameter("f", "json");
            request.send();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
