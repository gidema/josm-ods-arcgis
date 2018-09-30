package org.openstreetmap.josm.plugins.ods.arcgis.rest.config;

import java.util.Objects;

public class HostConfig {
    private final String url;
    private final String serviceName;
    private final ServiceType serviceType;

    public HostConfig(String url, String serviceName, ServiceType serviceType) {
        super();
        this.url = url;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
    }

    public String getUrl() {
        return url;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getServiceUrl() {
        return url + "/" + serviceName + "/" + serviceType.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, url, serviceType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof HostConfig && equals((HostConfig)obj);
    }

    private boolean equals(HostConfig other) {
        return serviceName.equals(other.serviceName) &&
                url.equals(other.url) &&
                serviceType.equals(other.serviceType);
    }
}
