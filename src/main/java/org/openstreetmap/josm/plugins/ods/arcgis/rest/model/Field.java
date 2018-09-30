package org.openstreetmap.josm.plugins.ods.arcgis.rest.model;

public class Field {
    private String name;
    private EsriFieldType type;
    private String alias;
    private String sqlType;
    private boolean nullable;
    private boolean editable;
    private String domain;
    private String defaultValue;

    public String getName() {
        return name;
    }

    public EsriFieldType getType() {
        return type;
    }

    public String getAlias() {
        return alias;
    }

    public String getSqlType() {
        return sqlType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getDomain() {
        return domain;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
