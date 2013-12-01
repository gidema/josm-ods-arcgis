package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.util.List;

import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.InitializationException;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.ServiceException;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.HostDescriptionParser;

public class AGRestHost extends Host {
  private boolean initialized = false;
  private List<String> featureTypes;

  @Override
  public void initialize() throws InitializationException {
    if (initialized) return;
    HttpRequest request = new HttpRequest();
    try {
      request.open("GET", getUrl());
      request.addParameter("f", "json");
      HttpResponse response = request.send();
      HostDescriptionParser.parseHostJson(response.getInputStream(), this);
      response.close();
      initialized = true;
    } catch (IOException e) {
      throw new InitializationException(e);
    }
  }
  
  public void setFeatureTypes(List<String> featureTypes) {
    this.featureTypes = featureTypes;
  }

  @Override
  public boolean hasFeatureType(String feature) {
    return featureTypes.contains(feature);
  }

  @Override
  public OdsFeatureSource getOdsFeatureSource(String feature) throws ServiceException {
    return new AGRestFeatureSource(this, feature);
  }
}
