package org.openstreetmap.josm.plugins.ods.arcgis.rest.config;

import java.util.Objects;

public class FSConfig {
    private final HostConfig hostConfig;
    private final Long featureId;

    public FSConfig(HostConfig hostConfig, Long featureId) {
        super();
        this.hostConfig = hostConfig;
        this.featureId = featureId;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public Long getFeatureId() {
        return featureId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostConfig, featureId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof FSConfig && equals((FSConfig)obj);
    }

    private boolean equals(FSConfig other) {
        return featureId.equals(other.featureId)
                && hostConfig.equals(other.hostConfig);
    }
}
