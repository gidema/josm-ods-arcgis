package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.ods.DownloadJob;
import org.openstreetmap.josm.plugins.ods.ImportDataLayer;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.BuildException;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.imported.ImportedEntityBuilder;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;
//import org.openstreetmap.josm.plugins.ods.crs.JTSCoordinateTransform;
//import org.openstreetmap.josm.plugins.ods.crs.JTSCoordinateTransformFactory;
//import org.openstreetmap.josm.plugins.ods.crs.Proj4jCRSTransformFactory;

public class AGRestDownloadJob implements DownloadJob {
    AGRestDataSource dataSource;
    AGRestFeatureSource featureSource;
    Bounds bounds;
    SimpleFeatureCollection featureCollection;
    MetaData metaData;
    ImportDataLayer dataLayer;
    Set<Entity> newEntities;

    public AGRestDownloadJob(AGRestDataSource dataSource, ImportDataLayer dataLayer, Bounds bounds, Set<Entity> newEntities) {
        this.dataSource = dataSource;
        this.dataLayer = dataLayer;
        this.bounds = bounds;
        this.newEntities = newEntities;
    }

    @Override
    public Callable<?> getPrepareCallable() {
        return new Callable<Object>() {

            @Override
            public Object call() throws ExecutionException {
                try {
                    dataSource.initialize();
                    metaData = dataSource.getMetaData();
                    featureSource = (AGRestFeatureSource) dataSource
                            .getOdsFeatureSource();
                } catch (Exception e) {
                    throw new ExecutionException(e.getMessage(), e.getCause());
                }
                return null;
            }

        };
    }

    @Override
    public Callable<?> getDownloadCallable() {
        return new Callable<Object>() {

            @Override
            public Object call() throws ExecutionException {
                List<SimpleFeature> featureList = new LinkedList<SimpleFeature>();
                try {
                    RestQuery query = getQuery();
                    AGRestReader reader = new AGRestReader(query,
                            featureSource.getFeatureType());
                    SimpleFeatureIterator it = reader.getFeatures().features();
                    while (it.hasNext()) {
                        featureList.add(it.next());
                    }
                } catch (Exception e) {
                    throw new ExecutionException(e.getMessage(), e.getCause());
                }
                ImportedEntityBuilder builder = dataSource.getEntityBuilder();
                try {
                    for (SimpleFeature feature : featureList) {
                        Entity entity = builder.build(feature);
                        if (dataLayer.getEntitySet().add(entity)) {
                            newEntities.add(entity);
                        };
                    }
                } catch (BuildException e) {
                    throw new ExecutionException(e.getMessage(), e.getCause());
                }
                return null;
            }
        };
    }

    // @Override
    // public OdsFeatureSet getFeatureSet() {
    // return featureSet;
    // }

    RestQuery getQuery() throws CRSException {
        RestQuery query = new RestQuery();
        query.setFeatureSource(featureSource);
        query.setInSR(featureSource.getSRID());
        query.setOutSR(featureSource.getSRID());
        query.setGeometry(formatBounds(bounds, query.getInSR()));
        query.setOutFields("*");
        return query;
    }

    private static String formatBounds(Bounds bounds, Long srid) throws CRSException {
        CRSUtil crsUtil = CRSUtil.getInstance();
        CoordinateReferenceSystem crs = crsUtil.getCrs(srid);
        ReferencedEnvelope envelope = crsUtil.createBoundingBox(crs, bounds);
        return String.format(Locale.ENGLISH, "%f,%f,%f,%f", envelope.getMinX(),
            envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
   }

    @Override
    public Set<Entity> getNewEntities() {
        return newEntities;
    }
}
