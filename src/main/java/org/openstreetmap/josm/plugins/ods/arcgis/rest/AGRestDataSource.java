package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.OdsDataSource;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.DSConfig;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;

public class AGRestDataSource implements OdsDataSource {
    private final AGRestFeatureSource featureSource;
    private final DSConfig dsConfig;
    private boolean initialized;
    private boolean required;

    public AGRestDataSource(AGRestFeatureSource fs, DSConfig dsConfig) {
        this.featureSource = fs;
        this.dsConfig = dsConfig;
    }

    @Override
    public final AGRestFeatureSource getFeatureSource() {
        return featureSource;
    }

    public void initialize() {
        if (!initialized) {
            featureSource.initialize();
            initialized = true;
        }
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    //    public RestQuery getQuery() {
    //        return query;
    //    }

    @Override
    public String getFeatureType() {
        return featureSource.getFeatureName();
    }

    @Override
    public MetaData getMetaData() {
        return getFeatureSource().getMetaData();
    }

    public RestQuery getQuery() {
        RestQuery query = new RestQuery();
        query.setHost(getFeatureSource().getHost().getServiceUrl().toString());
        query.setLayer(getFeatureSource().getFeatureId());
        if (dsConfig.getProperties().size() > 0) {
            query.setOutFields(String.join(",", dsConfig.getProperties()));
        }
        else {
            query.setOutFields("*");
        }
        query.setInSR(28992L);
        query.setOutSR(4326L);
        return query;
    }

    /**
     * Get the feature type for this datasource with the specified srsId
     * @param outSR
     * @return
     */
    public SimpleFeatureType getFeatureType(Long outSR) {
        try {
            CoordinateReferenceSystem crs = CRSUtil.getCrs(outSR);
            return SimpleFeatureTypeBuilder.retype((SimpleFeatureType) getFeatureSource().getFeatureType(), crs);
        } catch (CRSException e) {
            throw new RuntimeException(e);
        }
    }
}
