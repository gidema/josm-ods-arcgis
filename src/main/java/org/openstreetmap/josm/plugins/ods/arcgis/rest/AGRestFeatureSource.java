package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;

import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.FeatureTypeParser;
import org.openstreetmap.josm.plugins.ods.exceptions.OdsException;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;

public class AGRestFeatureSource implements OdsFeatureSource {
    private boolean initialized = false;
    private boolean available = false;
    private final AGRestHost host;
    private final String feature;
    private final Long featureId;
    private FeatureType featureType;
    private MetaData metaData;

    public AGRestFeatureSource(AGRestHost host, String feature) {
        super();
        this.host = host;
        this.feature = feature;
        String[] parts = feature.split("/");
        this.featureId = Long.valueOf(parts[1]);
    }

    @Override
    public final AGRestHost getHost() {
        return host;
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    @Override
    public String getIdAttribute() {
        return null;
    }

    @Override
    public final String getFeatureName() {
        return String.format("%s:%s", host.getName(), feature);
    }

    @Override
    public void initialize() throws OdsException {
        if (initialized) return;
        initialized = true;
        host.initialize();
        metaData = host.getMetaData();
        HttpRequest request = new HttpRequest();
        try {
            request.open("GET", host.getUrl() + "/" + featureId);
            request.addParameter("f", "json");
            HttpResponse response = request.send();
            FeatureTypeParser parser = new FeatureTypeParser();
            featureType = parser.parse(response.getInputStream(),
                host.getName());
        } catch (IOException e) {
            String msg = String.format("Feature '%s' is not available from host '%s' (%s)",
                featureId, host.getName(), host.getUrl().toString());
            throw new OdsException(msg);
        }
        available = true;
        return;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public MetaData getMetaData() {
        assert available;
        return metaData;
    }

    public Long getFeatureId() {
        return featureId;
    }

    @Override
    public CoordinateReferenceSystem getCrs() {
        return featureType.getGeometryDescriptor()
                .getCoordinateReferenceSystem();
    }

    @Override
    public String getSRS() {
        ReferenceIdentifier rid = getCrs().getIdentifiers().iterator().next();
        return rid.toString();
    }

    @Override
    public Long getSRID() {
        ReferenceIdentifier rid = getCrs().getIdentifiers().iterator().next();
        return Long.parseLong(rid.getCode());
    }
}
