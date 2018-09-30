package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.feature.type.FeatureType;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.AGRestConfig;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.DLConfig;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.DSConfig;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.FSConfig;
import org.openstreetmap.josm.plugins.ods.arcgis.rest.config.HostConfig;
import org.openstreetmap.josm.plugins.ods.crs.CRSUtil;
import org.openstreetmap.josm.plugins.ods.util.MessageBuilder;
import org.openstreetmap.josm.tools.Logging;

public class AGRestInitializer {
    private final Set<HostConfig> hostConfigs = new HashSet<>();
    private final HashSet<FSConfig> fsConfigs = new HashSet<>();
    private final HashSet<DSConfig> dsConfigs = new HashSet<>();

    private final Map<HostConfig, AGRestHost> hosts = new HashMap<>();
    private final Map<FSConfig, AGRestFeatureSource> featureSources = new HashMap<>();
    private final Map<DSConfig, AGRestDataSource> dataSources = new HashMap<>();

    private List<AGRestDownloader> downloaders;

    public void initialize(AGRestConfig config, CRSUtil crsUtil)
            throws IOException {
        config.getDownloaders().forEach(dlConfig -> {
            dsConfigs.add(dlConfig.getDsConfig());
        });
        dsConfigs.forEach(dsConfig -> {
            fsConfigs.add(dsConfig.getFsConfig());
        });
        fsConfigs.forEach(fsConfig -> {
            hostConfigs.add(fsConfig.getHostConfig());
        });
        MessageBuilder mb = new MessageBuilder();
        createHosts(mb);
        createFeatureSources(mb);
        createDataSources(mb);
        if (mb.length() > 0) {
            String message = mb.toString();
            Logging.warn(message);
            System.out.println(message);
        }
        this.downloaders = createDownloaders(config, crsUtil);
    }

    public List<AGRestDownloader> getDownloaders() {
        return downloaders;
    }

    private void createHosts(MessageBuilder mb) {
        hostConfigs.forEach(hostConfig -> {
            AGRestHost host = new AGRestHost(hostConfig);
            hosts.put(hostConfig, host);
            if (!host.initialize()) {
                mb.append(host.getErrorMessage()).append("\n");
            }
        });
    }

    private void createFeatureSources(MessageBuilder mb) throws IOException {
        for (FSConfig fsConfig : fsConfigs) {
            HostConfig cfg = fsConfig.getHostConfig();
            AGRestHost host = hosts.get(cfg);
            if (host.isAvailable()) {
                AGRestFeatureSource fs = host
                        .getFeatureSource(fsConfig.getFeatureId());
                if (fs == null) {
                    mb.appendI18n("Unknown feature index: {0} for host {1}.\n",
                            fsConfig.getFeatureId(), host.getName());
                } else {
                    featureSources.put(fsConfig, fs);
                }
            }
        }
    }

    private void createDataSources(MessageBuilder mb) {
        for (DSConfig dsConfig : dsConfigs) {
            FSConfig fsConfig = dsConfig.getFsConfig();
            AGRestFeatureSource fs = featureSources.get(fsConfig);
            if (fs != null) {
                List<String> unknown = getUnknownProperties(fs.getFeatureType(),
                        dsConfig.getProperties());
                if (unknown.size() > 0) {
                    mb.appendI18n("Unknown properties for feature {0}: {1}\n",
                            fs.getFeatureName(), String.join(",", unknown));
                } else {
                    AGRestDataSource ds = new AGRestDataSource(fs, dsConfig);
                    dataSources.put(dsConfig, ds);
                }
            }
        }
    }

    private static List<String> getUnknownProperties(FeatureType featureType,
            List<String> properties) {
        List<String> result = new LinkedList<>();
        for (String property : properties) {
            if (featureType.getDescriptor(property) == null) {
                result.add(property);
            }
        }
        return result;
    }

    private List<AGRestDownloader> createDownloaders(AGRestConfig config,
            CRSUtil crsUtil) {
        List<AGRestDownloader> result = new ArrayList<>(config.getDownloaders().size());
        for (DLConfig dlConfig : config.getDownloaders()) {
            DSConfig dsConfig = dlConfig.getDsConfig();
            AGRestDataSource ds = dataSources.get(dsConfig);
            result.add(new AGRestDownloader(ds, crsUtil,
                    dlConfig.getFeatureParser()));
        }
        return result;
    }

}
