package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.Locale;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.Normalisation;
import org.openstreetmap.josm.plugins.ods.OdsDataSource;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureDownloader;
import org.openstreetmap.josm.plugins.ods.io.DownloadRequest;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.io.Status;
import org.openstreetmap.josm.plugins.ods.parsing.FeatureParser;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

/**
 * Downloader for a single Arcgis rest feature.
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 * @param <T>
 */
public class AGRestDownloader<T extends Entity> implements FeatureDownloader {
    private final OdsDataSource dataSource;
    private final CRSUtil crsUtil;

    private AGRestFeatureSource featureSource;
    private DefaultFeatureCollection downloadedFeatures;
    private final FeatureParser parser;
    private DownloadRequest request;
    private final Status status = new Status();

    private DownloadResponse response;

    public AGRestDownloader(OdsDataSource dataSource, CRSUtil crsUtil, FeatureParser parser) {
        this.crsUtil = crsUtil;
        this.dataSource = dataSource;
        this.parser = parser;
    }

    @Override
    public void setNormalisation(Normalisation normalisation) {
        // Do nothing. This method will be deprecated anyway
    }

    @Override
    public void setup(DownloadRequest request) {
        this.request = request;
    }

    @Override
    public void setResponse(DownloadResponse response) {
        this.response = response;
    }

    @Override
    public void prepare() {
        featureSource = (AGRestFeatureSource) dataSource
                .getOdsFeatureSource();
        return;
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
        try (SimpleFeatureIterator it = reader.getFeatures().features();) {
            while (it.hasNext() && !Thread.currentThread().isInterrupted()) {
                downloadedFeatures.add(it.next());
            }
            if (Thread.currentThread().isInterrupted()) {
                downloadedFeatures.clear();
                return;
            }
        } catch (ArcgisServerRestException | NoSuchElementException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (downloadedFeatures.isEmpty() && dataSource.isRequired()) {
            String featureType = dataSource.getFeatureType();
            Logging.info(
                    I18n.tr("The selected download area contains no {0} objects.",
                            featureType));
        } else {
            Host host = dataSource.getOdsFeatureSource().getHost();
            host.getMaxFeatures();
            Integer maxFeatures = host.getMaxFeatures();
            if (maxFeatures != null
                    && downloadedFeatures.size() >= maxFeatures) {
                String featureType = dataSource.getFeatureType();
                throw new RuntimeException(
                        I18n.tr("To many {0} objects. Please choose a smaller download area.",
                                featureType));
            }
        }
    }

    @Override
    public void process() {
        parser.parse(downloadedFeatures, response);
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
        CoordinateReferenceSystem crs = CRSUtil.getCrs(srid);
        ReferencedEnvelope envelope = crsUtil.createBoundingBox(crs,
                request.getBoundary().getBounds());
        return String.format(Locale.ENGLISH, "%f,%f,%f,%f", envelope.getMinX(),
                envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }

    @Override
    public void cancel() {
        // No action required
    }
}
