package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.plugins.ods.AbstractOdsDataSource;
import org.openstreetmap.josm.plugins.ods.DownloadJob;
import org.openstreetmap.josm.plugins.ods.DownloadTask;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.entities.external.ExternalDataLayer;
import org.openstreetmap.josm.plugins.ods.entities.external.ExternalDownloadTask;

public class AGRestDataSource extends AbstractOdsDataSource {

  protected AGRestDataSource(OdsFeatureSource odsFeatureSource) {
    super(odsFeatureSource);
  }

  @Override
  public ExternalDownloadTask createDownloadTask(Bounds bounds) {
    return new AGRestDownloadTask(this, bounds);
  }
}
