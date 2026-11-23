package org.machanism.machai.gw.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public final class LoggingConfig {

    private static final String PREFIX = "org.slf4j.simpleLogger.";

    private LoggingConfig() {}

    /**
     * Loads SimpleLogger settings from a classpath resource and reinitializes SimpleLogger.
     *
     * @param resourceName     classpath resource, e.g. "simplelogger.properties"
     * @param overrideExisting if false, values already set via -D or System.setProperty win
     */
    public static void applyClasspathResource(String resourceName, boolean overrideExisting)
            throws IOException {
        Objects.requireNonNull(resourceName, "resourceName");
        String name = resourceName.startsWith("/") ? resourceName.substring(1) : resourceName;

        Properties props = load(name);

        props.stringPropertyNames().stream()
                .filter(k -> k.startsWith(PREFIX))
                .forEach(k -> {
                    if (overrideExisting || System.getProperty(k) == null) {
                        System.setProperty(k, props.getProperty(k).trim());
                    }
                });

        reinitialize();
    }

    public static void applyClasspathResource(String resourceName) throws IOException {
        applyClasspathResource(resourceName, false); // command-line -D flags win by default
    }

    private static Properties load(String name) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = LoggingConfig.class.getClassLoader();

        try (InputStream in = cl.getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Resource not found on classpath: " + name);
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        }
    }

    /** Makes SimpleLogger re-read System properties and drops cached Logger instances. */
    private static void reinitialize() {
        try {
            Class<?> simpleLogger = Class.forName("org.slf4j.simple.SimpleLogger");
            Method init = simpleLogger.getDeclaredMethod("init");
            init.setAccessible(true);
            init.invoke(null);

            // slf4j-simple 2.x: clear cached loggers so new ones pick up new levels
            ILoggerFactory factory = LoggerFactory.getILoggerFactory();
            try {
                Method reset = factory.getClass().getDeclaredMethod("reset");
                reset.setAccessible(true);
                reset.invoke(factory);
            } catch (NoSuchMethodException ignored) {
                // 1.7.x: no reset(); loggers created after this call still see the new config
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            System.err.println("SimpleLogger reinitialization failed: " + e);
        }
    }
}
