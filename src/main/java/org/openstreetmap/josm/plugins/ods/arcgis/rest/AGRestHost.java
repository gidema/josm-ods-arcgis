package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.ods.OdsDataSource;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.OdsModule;
import org.openstreetmap.josm.plugins.ods.ServiceException;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.HostDescriptionParser;
import org.openstreetmap.josm.plugins.ods.entities.Entity;
import org.openstreetmap.josm.plugins.ods.entities.opendata.FeatureDownloader;
import org.openstreetmap.josm.plugins.ods.exceptions.OdsException;
import org.openstreetmap.josm.plugins.ods.io.AbstractHost;
import org.openstreetmap.josm.tools.I18n;

/**
 * @Host implementation for ArcGist REST services.
 * 
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class AGRestHost extends AbstractHost {
    private List<String> featureTypes;

    public AGRestHost(String name, String url, Integer maxFeatures) {
        super(name, url, maxFeatures);
    }

    @Override
    public synchronized void initialize() throws OdsException {
        if (isInitialized()) return;
        super.initialize();
        HttpRequest request = new HttpRequest();
        try {
            request.open("GET", getUrl().toString());
            request.addParameter("f", "json");
            HttpResponse response = request.send();
            HostDescriptionParser.parseHostJson(response.getInputStream(),
                this);
            response.close();
        } catch (UnknownHostException e) {
            String msg = I18n.tr("Host ''{0}'' ({1)) is not available. Please check your Internet connection.",
                    getName(), getUrl().toString());
            setInitialized(false);
            throw new OdsException(msg);
        } catch (IOException e) {
            String msg = I18n.tr("Host ''{0}'' ({1)) could not be initialized",
                    getName(), getUrl().toString());
            throw new OdsException(msg);
        }
        for (String featureType : featureTypes) {
            List<String> messages = new LinkedList<>();
            try {
                getOdsFeatureSource(featureType);
            } catch (ServiceException e) {
                Main.error(e);
                messages.add(e.getMessage());
            }
            if (!messages.isEmpty()) {
                throw new OdsException(messages);
            }
        }
        setInitialized(true);
        return;
    }

    public void setFeatureTypes(List<String> featureTypes) {
        this.featureTypes = featureTypes;
    }

    @Override
    public boolean hasFeatureType(String feature) {
        return featureTypes.contains(feature);
    }

    @Override
    public OdsFeatureSource getOdsFeatureSource(String feature)
            throws ServiceException {
        return new AGRestFeatureSource(this, feature);
    }
    
    @Override
    public <T extends Entity> FeatureDownloader createDownloader(OdsModule module, OdsDataSource dataSource, Class<T> clazz) throws OdsException {
        dataSource.initialize();
        return new AGRestDownloader<>(module, dataSource);
    }

}
