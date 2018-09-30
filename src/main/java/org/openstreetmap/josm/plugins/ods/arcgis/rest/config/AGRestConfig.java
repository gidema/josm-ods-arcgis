package org.openstreetmap.josm.plugins.ods.arcgis.rest.config;

import java.util.List;

import org.openstreetmap.josm.plugins.ods.entities.EntityPrimitiveBuilder;

public abstract class AGRestConfig {
    public abstract List<DLConfig> getDownloaders();

    public abstract List<EntityPrimitiveBuilder<?>> getPrimitiveBuilders();
}
