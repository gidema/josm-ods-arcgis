package org.openstreetmap.josm.plugins.ods.arcgis.rest.model;

import org.openstreetmap.josm.plugins.ods.arcgis.rest.SpatialReference;

public class Extent {
    private double xmin;
    private double ymin;
    private double xmax;
    private double ymax;
    private SpatialReference spatialReference;

    public double getXmin() {
        return xmin;
    }

    public double getYmin() {
        return ymin;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmax() {
        return ymax;
    }

    public SpatialReference getSpatialReference() {
        return spatialReference;
    }

    public String getSrs() {
        return "EPSG:" + getSpatialReference().getWkid();
    }
}
