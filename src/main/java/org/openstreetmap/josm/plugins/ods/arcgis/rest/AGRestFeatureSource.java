package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.FeatureTypeFactory;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.AGRestFeatureLayer;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;

public class AGRestFeatureSource implements OdsFeatureSource {
    private final boolean available = false;
    private final AGRestHost host;
    private final AGRestFeatureLayer featureLayer;
    private final FeatureType featureType;
    private MetaData metaData;

    public AGRestFeatureSource(AGRestHost host, AGRestFeatureLayer featureLayer) {
        super();
        this.host = host;
        this.featureLayer = featureLayer;
        this.featureType = FeatureTypeFactory.createFeatureType(featureLayer, null);
    }

    public AGRestHost getHost() {
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
        return String.format("%s:%s", host.getName(), featureLayer.getName());
    }

    public String getUrl() {
        return host.getServiceUrl().toString() + "/" + getFeatureId().toString();
    }

    @Override
    public void initialize() {
        return;
    }

    @Override
    public MetaData getMetaData() {
        assert available;
        return metaData;
    }

    public AGRestFeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    public Long getFeatureId() {
        return featureLayer.getId();
    }

    @Override
    public CoordinateReferenceSystem getCrs() {
        return getFeatureType().getGeometryDescriptor()
                .getCoordinateReferenceSystem();
    }

    @Override
    public String getSRS() {
        ReferenceIdentifier rid = getCrs().getIdentifiers().iterator().next();
        return rid.toString();
    }

    @Override
    public Long getSRID() {
        return new Long(featureLayer.getExtent().getSpatialReference().getWkid());
    }

    public Integer getMaxFeatures() {
        return featureLayer.getMaxRecordCount();
    }
}
