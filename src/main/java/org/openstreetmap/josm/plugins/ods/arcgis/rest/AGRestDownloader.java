package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.Context;
import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.EntitySource;
import org.openstreetmap.josm.plugins.ods.entities.EntityStore;
import org.openstreetmap.josm.plugins.ods.entities.external.GeotoolsEntityBuilder;
import org.openstreetmap.josm.plugins.ods.io.Downloader;
import org.openstreetmap.josm.plugins.ods.io.Status;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;
import org.openstreetmap.josm.plugins.ods.tasks.Task;
import org.openstreetmap.josm.tools.I18n;

public class AGRestDownloader implements Downloader {
    private final AGRestDataSource dataSource;
    private final CRSUtil crsUtil;
    private final GeotoolsEntityBuilder<?> entityBuilder;
    private final EntityStore<?> entityStore;
    private final List<Task> tasks;
    private EntitySource entitySource;

    AGRestFeatureSource featureSource;
    SimpleFeatureCollection featureCollection;
    MetaData metaData;
    private final Status status = new Status();
    private DefaultFeatureCollection downloadedFeatures;
    private Context ctx;

    public AGRestDownloader(AGRestDataSource dataSource, CRSUtil crsUtil,
            GeotoolsEntityBuilder<?> entityBuilder, EntityStore<?> entityStore,
            List<Task> tasks) {
        this.crsUtil = crsUtil;
        this.dataSource = dataSource;
        this.entityBuilder = entityBuilder;
        this.entityStore = entityStore;
        this.tasks = tasks;
    }

    RestQuery getQuery() throws CRSException {
        RestQuery query = new RestQuery();
        query.setFeatureSource(featureSource);
        query.setInSR(featureSource.getSRID());
        query.setOutSR(featureSource.getSRID());
        query.setGeometry(formatBounds(query.getInSR()));
        query.setOutFields("*");
        return query;
    }

    private String formatBounds(Long srid) throws CRSException {
        CoordinateReferenceSystem crs = crsUtil.getCrs(srid);
        ReferencedEnvelope envelope = crsUtil.createBoundingBox(crs,
                entitySource.getBoundary().getBounds());
        return String.format(Locale.ENGLISH, "%f,%f,%f,%f", envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

//    @Override
//    public void setBoundary(Boundary boundary) {
//        this.boundary = boundary;
//    }
//
    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void prepare(Context ctxt) throws InterruptedException {
        this.ctx = ctxt;
        try {
            dataSource.initialize();
            metaData = dataSource.getMetaData();
            featureSource = (AGRestFeatureSource) dataSource
                    .getOdsFeatureSource();
            entitySource = (EntitySource) ctx.get("entitySource");
        } catch (Exception e) {
            status.setFailed(true);
            status.setException(e);
        }
    }

    // @Override
    // public void download() throws InterruptedException {
    // features = new LinkedList<>();
    // try {
    // } catch (Exception e) {
    // status.setFailed(true);
    // status.setException(e);
    // }
    // }

    @Override
    public void download() throws InterruptedException {
        downloadedFeatures = new DefaultFeatureCollection();
        try {
            RestQuery query = getQuery();
            AGRestReader reader = new AGRestReader(query,
                    featureSource.getFeatureType());
            SimpleFeatureIterator it = reader.getFeatures().features();
            while (it.hasNext()) {
                downloadedFeatures.add(it.next());
            }
        } catch (ArcgisServerRestException|NoSuchElementException|CRSException e) {
            status.setFailed(true);
            status.setException(e);
            throw new InterruptedException(e.getMessage());
        }
        if (status.isCancelled()) {
            return;
        }
        if (downloadedFeatures.isEmpty() && dataSource.isRequired()) {
            String featureType = dataSource.getFeatureType();
            status.setMessage(I18n.tr(
                    "The selected download area contains no {0} objects.",
                    featureType));
            status.setCancelled(true);
        } else {
            Host host = dataSource.getOdsFeatureSource().getHost();
            host.getMaxFeatures();
            Integer maxFeatures = host.getMaxFeatures();
            if (maxFeatures != null && downloadedFeatures.size() >= maxFeatures) {
                String featureType = dataSource.getFeatureType();
                status.setMessage(I18n
                        .tr("To many {0} objects. Please choose a smaller download area.",
                                featureType));
                status.setCancelled(true);
            }
        }
        if (!status.isSucces()) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    @Override
    public void process() {
        entityBuilder.setMetaData(metaData);
        for (SimpleFeature feature : downloadedFeatures) {
             entityBuilder.buildGtEntity(feature);
        }
        entityStore.extendBoundary(entitySource.getBoundary().getMultiPolygon());
        for (Task task : tasks) {
            task.run(ctx);
        }
    }

    @Override
    public void cancel() {
        // TODO Auto-generated method stub
    }
}
