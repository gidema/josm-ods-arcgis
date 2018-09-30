package org.openstreetmap.josm.plugins.ods.arcgis.rest.model;

import java.util.List;

public class AGRestFeatureLayer {
    private Long id;
    private String name;
    private String description;
    private EsriGeometryType geometryType;
    private Extent extent;
    private String objectIdField;
    private List<Field> fields;
    private Integer maxRecordCount;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EsriGeometryType getGeometryType() {
        return geometryType;
    }

    public Extent getExtent() {
        return extent;
    }

    public String getObjectIdField() {
        return objectIdField;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Integer getMaxRecordCount() {
        return maxRecordCount;
    }
}
