package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.Locale;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureDownloader;
import org.openstreetmap.josm.plugins.ods.entities.opendata.GeotoolsEntityBuilder;
import org.openstreetmap.josm.plugins.ods.io.DownloadRequest;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.io.Status;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;
import org.openstreetmap.josm.tools.I18n;

public class AGRestDownloader implements FeatureDownloader {
    private final AGRestDataSource dataSource;
    private final CRSUtil crsUtil;
    private final GeotoolsEntityBuilder<?> entityBuilder;

    private AGRestFeatureSource featureSource;
    private MetaData metaData;
    private final Status status = new Status();
    private DefaultFeatureCollection downloadedFeatures;
    private DownloadRequest request;

    public AGRestDownloader(AGRestDataSource dataSource, CRSUtil crsUtil,
            GeotoolsEntityBuilder<?> entityBuilder) {
        this.crsUtil = crsUtil;
        this.dataSource = dataSource;
        this.entityBuilder = entityBuilder;
    }

    @Override
    public void setup(DownloadRequest request) {
        this.request = request;
    }

    @Override
    public void setResponse(DownloadResponse response) {
        // TODO Auto-generated method stub
    }

    @Override
    public void prepare() {
        try {
            dataSource.initialize();
            metaData = dataSource.getMetaData();
            featureSource = (AGRestFeatureSource) dataSource
                    .getOdsFeatureSource();
        } catch (Exception e) {
            status.setFailed(true);
            status.setException(e);
        }
    }

    @Override
    public void download() {
        downloadedFeatures = new DefaultFeatureCollection();
        RestQuery query;
        try {
            query = getQuery();
        } catch (CRSException e) {
            throw new RuntimeException(e);
        }
        AGRestReader reader = new AGRestReader(query,
                featureSource.getFeatureType());
        try (
            SimpleFeatureIterator it = reader.getFeatures().features();
        )  {
            while (it.hasNext()) {
                downloadedFeatures.add(it.next());
            }
        } catch (ArcgisServerRestException|NoSuchElementException e) {
            status.setFailed(true);
            status.setException(e);
            throw new RuntimeException(e.getMessage());
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
        for (SimpleFeature feature : downloadedFeatures) {
             entityBuilder.build(feature, metaData, null);
        }
    }

  @Override
  public Status getStatus() {
      return status;
  }


    private RestQuery getQuery() throws CRSException {
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
                request.getBoundary().getBounds());
        return String.format(Locale.ENGLISH, "%f,%f,%f,%f", envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }


    @Override
    public void cancel() {
        // TODO Auto-generated method stub
    }
}
