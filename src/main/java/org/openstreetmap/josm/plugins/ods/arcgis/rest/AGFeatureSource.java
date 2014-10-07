package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public class AGFeatureSource implements SimpleFeatureSource {

    @Override
    public Name getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceInfo getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFeatureListener(FeatureListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public SimpleFeatureType getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReferencedEnvelope getBounds() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReferencedEnvelope getBounds(Query query) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getCount(Query query) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<Key> getSupportedHints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleFeatureCollection getFeatures(Filter filter)
            throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
