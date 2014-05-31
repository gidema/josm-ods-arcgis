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
import org.openstreetmap.josm.plugins.ods.DownloadTask;
import org.openstreetmap.josm.plugins.ods.crs.CRSException;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.jts.Boundary;
import org.openstreetmap.josm.plugins.ods.metadata.MetaData;

public class AGRestDownloadTask implements DownloadTask {
	AGRestDataSource dataSource;
	AGRestFeatureSource featureSource;
	Boundary boundary;
	SimpleFeatureCollection featureCollection;
	MetaData metaData;
	List<SimpleFeature> features;
	Set<Entity> newEntities;
	private boolean cancelled = false;
	boolean failed = false;
	String message;
	Exception exception = null;

	public AGRestDownloadTask(AGRestDataSource dataSource, Boundary boundary) {
		this.dataSource = dataSource;
		this.boundary = boundary;
	}

	@Override
	public boolean cancelled() {
		return cancelled;
	}

	@Override
	public boolean failed() {
		return failed;
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Override
	public String getMessage() {
		if (message != null) {
			return message;
		}
		if (exception != null) {
			return exception.getMessage();
		}
		return null;
	}

	@Override
	public Callable<Object> stage(String subTask) {
		switch (subTask) {
		case "prepare":
			return new PrepareSubTask();
		case "download":
			return new DownloadSubTask();
		}
		return null;
	}

	class PrepareSubTask implements Callable<Object> {
		@Override
		public Object call() {
			try {
				dataSource.initialize();
				metaData = dataSource.getMetaData();
				featureSource = (AGRestFeatureSource) dataSource
						.getOdsFeatureSource();
			} catch (Exception e) {
				failed = true;
				exception = e;
			}
			return null;
		}
	}

	class DownloadSubTask implements Callable<Object> {
		@Override
		public Object call() throws ExecutionException {
			features = new LinkedList<>();
			try {
				RestQuery query = getQuery();
				AGRestReader reader = new AGRestReader(query,
						featureSource.getFeatureType());
				SimpleFeatureIterator it = reader.getFeatures().features();
				while (it.hasNext()) {
					features.add(it.next());
				}
			} catch (Exception e) {
				throw new ExecutionException(e.getMessage(), e.getCause());
			}
			return null;
		}
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
		query.setGeometry(formatBounds(boundary, query.getInSR()));
		query.setOutFields("*");
		return query;
	}

	private static String formatBounds(Boundary boundary, Long srid)
			throws CRSException {
		CRSUtil crsUtil = CRSUtil.getInstance();
		CoordinateReferenceSystem crs = crsUtil.getCrs(srid);
		ReferencedEnvelope envelope = crsUtil.createBoundingBox(crs,
				boundary.getBounds());
		return String.format(Locale.ENGLISH, "%f,%f,%f,%f", envelope.getMinX(),
				envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
	}
}
