package org.openstreetmap.josm.plugins.ods.arcgis.rest.config;

import org.openstreetmap.josm.plugins.ods.parsing.FeatureParser;

public class DLConfig {
    private final DSConfig dsConfig;
    private final FeatureParser featureParser;

    public DLConfig(DSConfig dsConfig, FeatureParser featureParser) {
        super();
        this.dsConfig = dsConfig;
        this.featureParser = featureParser;
    }

    public DSConfig getDsConfig() {
        return dsConfig;
    }

    public FeatureParser getFeatureParser() {
        return featureParser;
    }
}
