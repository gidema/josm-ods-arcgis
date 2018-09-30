package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AGRestDatasourceBuilder {
    private AGRestFeatureSource featureSource;
    private List<String> properties = Collections.emptyList();
    private int pageSize = 0;

    public void setFeatureSource(AGRestFeatureSource featureSource) {
        this.featureSource = featureSource;
    }

    public void setProperties(String ... properties) {
        this.properties = Arrays.asList(properties);
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public AGRestDataSource build() {
        RestQuery query = new RestQuery();
        if (properties.size() > 0) {
            query.setOutFields(String.join(",", properties));
        }
        query.setOutSR(4326L);
        return null;
        //        return new AGRestDataSource(featureSource, query);
    }
}
