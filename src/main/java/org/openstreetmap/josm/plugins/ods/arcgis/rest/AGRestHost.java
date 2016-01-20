package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.util.List;

import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.ServiceException;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.HostDescriptionParser;
import org.openstreetmap.josm.plugins.ods.io.AbstractHost;

import exceptions.OdsException;

public class AGRestHost extends AbstractHost {
    private List<String> featureTypes;

    public AGRestHost(String name, String url, Integer maxFeatures) {
        super(name, url, maxFeatures);
    }

    @Override
    public synchronized void initialize() throws OdsException {
        if (isInitialized()) return;
        super.initialize();
        HttpRequest request = new HttpRequest();
        try {
            request.open("GET", getUrl().toString());
            request.addParameter("f", "json");
            HttpResponse response = request.send();
            HostDescriptionParser.parseHostJson(response.getInputStream(),
                this);
            response.close();
        } catch (@SuppressWarnings("unused") IOException e) {
            String msg = String.format("Host '%s' (%s) could not be initialized",
                    getName(), getUrl().toString());
            throw new OdsException(msg);
        }
        setAvailable(true);
        return;
    }

    public void setFeatureTypes(List<String> featureTypes) {
        this.featureTypes = featureTypes;
    }

    @Override
    public boolean hasFeatureType(String feature) {
        return featureTypes.contains(feature);
    }

    @Override
    public OdsFeatureSource getOdsFeatureSource(String feature)
            throws ServiceException {
        return new AGRestFeatureSource(this, feature);
    }
}
