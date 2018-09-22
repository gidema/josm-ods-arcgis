package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.plugins.ods.Host;
import org.openstreetmap.josm.plugins.ods.InitializationException;
import org.openstreetmap.josm.plugins.ods.OdsFeatureSource;
import org.openstreetmap.josm.plugins.ods.ServiceException;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.json.HostDescriptionParser;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.Logging;

/**
 * @Host implementation for ArcGist REST services.
 *
 * @author Gertjan Idema <mail@gertjanidema.nl>
 *
 */
public class AGRestHost extends Host {
    private List<String> featureTypes;

    public AGRestHost(String name, String url, Integer maxFeatures) {
        super(name, url, maxFeatures);
    }

    @Override
    public synchronized void initialize() throws InitializationException {
        if (isInitialized()) return;
        super.initialize();
        setInitialized(false);
        try (
                HttpRequest request = new HttpRequest();
                )  {
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
            throw new InitializationException(msg);
        } catch (IOException e) {
            String msg = I18n.tr("Host ''{0}'' ({1}) could not be initialized because:\n{2}",
                    getName(), getUrl().toString(), e.getMessage());
            throw new InitializationException(msg);
        }
        for (String featureType : featureTypes) {
            List<String> messages = new LinkedList<>();
            try {
                getOdsFeatureSource(featureType);
            } catch (ServiceException e) {
                Logging.error(e);
                messages.add(e.getMessage());
            }
            if (!messages.isEmpty()) {
                throw new InitializationException(messages.toString());
            }
        }
        setInitialized(true);
        return;
    }

    public void setFeatureTypes(List<String> featureTypes) {
        this.featureTypes = featureTypes;
    }

    @Override
    public OdsFeatureSource getOdsFeatureSource(String feature)
            throws ServiceException {
        return new AGRestFeatureSource(this, feature);
    }

    //    @Override
    //    public <T extends Entity> FeatureDownloader createDownloader(OdsModule module, OdsDataSource dataSource, Class<T> clazz) throws OdsException {
    //        dataSource.initialize();
    //        return new AGRestDownloader<>(module, dataSource);
    //    }
    //
}
