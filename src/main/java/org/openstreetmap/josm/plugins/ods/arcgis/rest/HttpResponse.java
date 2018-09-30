package org.openstreetmap.josm.plugins.ods.arcgis.rest;

import java.io.IOException;
import java.io.InputStream;

public class HttpResponse implements AutoCloseable {
    private final HttpRequest request;

    public HttpResponse(HttpRequest request) {
        this.request = request;
    }

    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public void close() {
        request.close();
    }
}
