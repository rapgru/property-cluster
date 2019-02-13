package com.github.rapgru.propertycluster.configuration;

public class PropertyClusterConfigException extends RuntimeException {
    public PropertyClusterConfigException() {
    }

    public PropertyClusterConfigException(String message) {
        super(message);
    }

    public PropertyClusterConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyClusterConfigException(Throwable cause) {
        super(cause);
    }
}
