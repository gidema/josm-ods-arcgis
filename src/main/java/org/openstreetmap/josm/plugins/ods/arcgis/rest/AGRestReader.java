package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.FeatureCollectionParser;

public class AGRestReader {
    private final RestQuery query;
    private final FeatureCollectionParser parser;
    private final AGRestDataSource dataSource;

    public AGRestReader(AGRestDataSource dataSource, RestQuery query) {
        this.query = query;
        this.dataSource = dataSource;
        SimpleFeatureType featureType = dataSource.getFeatureType(query.getOutSR());
        this.parser = new FeatureCollectionParser(featureType);
    }

    public SimpleFeatureCollection getFeatures()
            throws ArcgisServerRestException {
        try (
                HttpRequest request = new HttpRequest();
                ) {
            AGRestFeatureSource featureSource = dataSource.getFeatureSource();
            String url = String.format("%s/%d/query",
                    featureSource.getHost().getServiceUrl(),
                    featureSource.getFeatureId());
            request.open("GET", url);
            request.addParameter("f", "json");
            request.addParameter("text", query.getText());
            request.addParameter("geometry", query.getGeometry());
            request.addParameter("geometryType",
                    query.getGeometryType().toString());
            request.addParameter("inSR", query.getInSR().toString());
            request.addParameter("spatialRef",
                    query.getSpatialRel().toString());
            request.addParameter("where", query.getWhere());
            request.addParameter("outfields", query.getOutFields());
            request.addParameter("returnGeometry",
                    query.getReturnGeometry().toString());
            if (query.getOutSR() != null) {
                request.addParameter("outSR", query.getOutSR().toString());
            }
            SimpleFeatureCollection features;
            try (HttpResponse response = request.send()) {
                features = parser.parse(response.getInputStream());
            }
            return features;
        } catch (MalformedURLException e) {
            throw new ArcgisServerRestException(e);
        } catch (IOException e) {
            throw new ArcgisServerRestException(e);
        }
    }
}
