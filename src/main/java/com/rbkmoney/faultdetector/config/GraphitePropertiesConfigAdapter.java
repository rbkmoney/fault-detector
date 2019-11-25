package com.rbkmoney.faultdetector.config;

import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteProtocol;
import org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class GraphitePropertiesConfigAdapter extends PropertiesConfigAdapter<GraphiteProperties>
        implements GraphiteConfig {

    GraphitePropertiesConfigAdapter(GraphiteProperties properties) {
        super(properties);
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public boolean enabled() {
        return get(GraphiteProperties::isEnabled, GraphiteConfig.super::enabled);
    }

    @Override
    public Duration step() {
        return get(GraphiteProperties::getStep, GraphiteConfig.super::step);
    }

    @Override
    public TimeUnit rateUnits() {
        return get(GraphiteProperties::getRateUnits, GraphiteConfig.super::rateUnits);
    }

    @Override
    public TimeUnit durationUnits() {
        return get(GraphiteProperties::getDurationUnits,
                GraphiteConfig.super::durationUnits);
    }

    @Override
    public String host() {
        return get(GraphiteProperties::getHost, GraphiteConfig.super::host);
    }

    @Override
    public int port() {
        return get(GraphiteProperties::getPort, GraphiteConfig.super::port);
    }

    @Override
    public GraphiteProtocol protocol() {
        return get(GraphiteProperties::getProtocol, GraphiteConfig.super::protocol);
    }

    @Override
    public String[] tagsAsPrefix() {
        return get(GraphiteProperties::getTagsAsPrefix,
                GraphiteConfig.super::tagsAsPrefix);
    }

}
