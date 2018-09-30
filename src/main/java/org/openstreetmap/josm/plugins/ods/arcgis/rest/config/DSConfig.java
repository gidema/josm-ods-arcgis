package org.openstreetmap.josm.plugins.ods.arcgis.rest.config;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DSConfig {
    private final FSConfig fsConfig;
    private final List<String> properties;

    public DSConfig(FSConfig fsConfig) {
        this(fsConfig, Collections.emptyList());
    }

    public DSConfig(FSConfig fsConfig, List<String> properties) {
        super();
        this.fsConfig = fsConfig;
        this.properties = properties == null ? Collections.emptyList() : properties;
    }

    public FSConfig getFsConfig() {
        return fsConfig;
    }

    public List<String> getProperties() {
        return properties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fsConfig, properties);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof DSConfig && equals((DSConfig)obj);
    }

    private boolean equals(DSConfig other) {
        return fsConfig.equals(other.fsConfig) && properties.equals(other.properties);
    }
}
