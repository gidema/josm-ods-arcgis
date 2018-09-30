package org.openstreetmap.josm.plugins.ods.arcgis.rest.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.vividsolutions.jts.geom.Geometry;

public enum EsriFieldType {
    SmallInteger("esriFieldTypeSmallInteger", Short.class),
    Integer("esriFieldTypeInteger", Integer.class),
    Single("esriFieldTypeSingle", Double.class),
    Double("esriFieldTypeDouble", Double.class),
    String("esriFieldTypeString", String.class),
    Date("esriFieldTypeDate", Date.class),
    OID("esriFieldTypeOID", Integer.class),
    Geometry("esriFieldTypeGeometry", Geometry.class),
    Blob("esriFieldTypeBlob", null),
    GUID("esriFieldTypeGUID", String.class),
    GlobalID("esriFieldTypeGlobalID", String.class);

    private String name;
    private Class<?> javaClass;

    EsriFieldType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    public String getName() {
        return name;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    @JsonCreator
    public static EsriFieldType parse(String name) {
        switch (name) {
        case "esriFieldTypeSmallInteger": return SmallInteger;
        case "esriFieldTypeInteger": return Integer;
        case "esriFieldTypeSingle": return Single;
        case "esriFieldTypeDouble": return Double;
        case "esriFieldTypeString": return String;
        case "esriFieldTypeDate": return Date;
        case "esriFieldTypeOID": return OID;
        case "esriFieldTypeGeometry": return Geometry;
        case "esriFieldTypeBlob": return Blob;
        case "esriFieldTypeGUID": return GUID;
        case "esriFieldTypeGlobalID": return GlobalID;
        default:
            throw new IllegalArgumentException("Unknown esri field type: " + name);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
