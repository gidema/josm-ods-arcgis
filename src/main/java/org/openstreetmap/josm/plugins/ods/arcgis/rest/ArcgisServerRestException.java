package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import org.openstreetmap.josm.plugins.ods.ServiceException;

public class ArcgisServerRestException extends ServiceException {

  /**
   * 
   */
  private static final long serialVersionUID = -6106853460949631324L;

  public ArcgisServerRestException(String message) {
    super(message);
  }

  public ArcgisServerRestException(Throwable cause) {
    super(cause);
  }

  public ArcgisServerRestException(String message, Throwable cause) {
    super(message, cause);
  }

}
