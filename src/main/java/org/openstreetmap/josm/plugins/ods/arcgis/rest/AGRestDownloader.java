package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.util.Locale;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.ods.Normalisation;
import org.openstreetmap.josm.plugins.ods.OdsDataSource;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureDownloader;
import org.openstreetmap.josm.plugins.ods.exceptions.OdsException;
import org.openstreetmap.josm.plugins.ods.io.DefaultPrepareResponse;
import org.openstreetmap.josm.plugins.ods.io.DownloadRequest;
import org.openstreetmap.josm.plugins.ods.io.DownloadResponse;
import org.openstreetmap.josm.plugins.ods.io.Host;
import org.openstreetmap.josm.plugins.ods.io.PrepareResponse;
import org.openstreetmap.josm.plugins.ods.properties.EntityMapper;
import org.openstreetmap.josm.plugins.ods.storage.Repository;
import org.openstreetmap.josm.tools.I18n;

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
    private final EntityMapper<SimpleFeature, T> mapper;
    private DefaultFeatureCollection downloadedFeatures;
    private final Repository repository;
    private DownloadRequest request;

    @SuppressWarnings("unused")
    private Normalisation normalisation;
    @SuppressWarnings("unused")
    private DownloadResponse response;

    @SuppressWarnings("unchecked")
    public AGRestDownloader(OdsModule module, OdsDataSource dataSource) {
        this.crsUtil = module.getCrsUtil();
        this.dataSource = dataSource;
        this.repository = module.getOpenDataLayerManager().getRepository();
        this.mapper = (EntityMapper<SimpleFeature, T>) dataSource
                .getEntityMapper();
    }

    @Override
    public void setNormalisation(Normalisation normalisation) {
        this.normalisation = normalisation;
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
    public PrepareResponse prepare() throws OdsException {
        featureSource = (AGRestFeatureSource) dataSource
                    .getOdsFeatureSource();
        return new DefaultPrepareResponse();
    }

    @Override
    public void download() throws OdsException {
        downloadedFeatures = new DefaultFeatureCollection();
        RestQuery query;
        try {
            query = getQuery();
        } catch (CRSException e) {
            throw new OdsException(e);
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
            throw new OdsException(e.getMessage(), e);
        }
        if (downloadedFeatures.isEmpty() && dataSource.isRequired()) {
            String featureType = dataSource.getFeatureType();
            Main.info(
                I18n.tr("The selected download area contains no {0} objects.",
                            featureType));
        } else {
            Host host = dataSource.getOdsFeatureSource().getHost();
            host.getMaxFeatures();
            Integer maxFeatures = host.getMaxFeatures();
            if (maxFeatures != null
                    && downloadedFeatures.size() >= maxFeatures) {
                String featureType = dataSource.getFeatureType();
                throw new OdsException(
                        I18n.tr("To many {0} objects. Please choose a smaller download area.",
                                featureType));
            }
        }
    }

    @Override
    public void process() {
        for (SimpleFeature feature : downloadedFeatures) {
            if (Thread.currentThread().isInterrupted()) {
                downloadedFeatures.clear();
                repository.clear();
                return;
            }
            T entity = mapper.map(feature);
            entity.setIncomplete(false);
            repository.add(entity);
        }
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
