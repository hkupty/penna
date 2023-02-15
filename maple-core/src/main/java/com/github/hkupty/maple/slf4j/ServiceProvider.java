package com.github.hkupty.maple.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class ServiceProvider implements SLF4JServiceProvider {

    public static String REQUESTED_API_VERSION = "2.0.99";
    private transient LoggerFactory loggerFactory;

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return new BasicMarkerFactory();
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return new BasicMDCAdapter();
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        loggerFactory = new LoggerFactory();
    }
}
