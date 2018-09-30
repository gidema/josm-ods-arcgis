package org.openstreetmap.josm.plugins.ods.arcgis.rest.json;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.FeatureType;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.AGRestFeatureLayer;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.EsriFieldType;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.model.Field;

/**
 * Factory to create an opengis FeatureType for an arcgis feature layer
 *
 * @author Gertjan Idema
 *
 */
public class FeatureTypeFactory {
    private final static String GEOMETRY_FIELD = "geometry";

    public static FeatureType createFeatureType(AGRestFeatureLayer layer, Long srsId) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        String description = layer.getDescription();
        String featureName = layer.getName() + "/" + layer.getId();
        //        if (namePrefix != null) {
        //            featureName = namePrefix + ":" + featureName;
        //        }
        String srs = (srsId == null ? layer.getExtent().getSrs() : "EPSG:" + srsId.toString());
        typeBuilder.setName(featureName);
        typeBuilder.setSRS(srs);
        typeBuilder.setDescription(new SimpleInternationalString(description));
        for (Field field :layer.getFields()) {
            EsriFieldType type = field.getType();
            if (type == EsriFieldType.Geometry) {
                Class<?> binding = layer.getGeometryType().getBinding();
                typeBuilder.add(field.getName(), binding, srs);
                typeBuilder.setDefaultGeometry(field.getName());
            }
            else {
                typeBuilder.add(field.getName(), field.getType().getJavaClass());
            }
        }
        if (typeBuilder.getDefaultGeometry() == null) {
            Class<?> binding = layer.getGeometryType().getBinding();
            typeBuilder.add(GEOMETRY_FIELD, binding);
            typeBuilder.setDefaultGeometry(GEOMETRY_FIELD);
        }
        return typeBuilder.buildFeatureType();
    }
}
