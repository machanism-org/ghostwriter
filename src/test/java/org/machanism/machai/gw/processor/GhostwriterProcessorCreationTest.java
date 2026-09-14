package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;

/** Covers CLI processor wiring without invoking external AI providers. */
class GhostwriterProcessorCreationTest {

    @TempDir
    Path tempDir;

    @Test
    void createProcessor_buildsGuidanceProcessorWhenActOptionIsAbsent() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        Object settings = settings("model", null);
        CommandLine commandLine = parse();

        // Act
        AIFileProcessor processor = (AIFileProcessor) invoke("createProcessor",
                new Class<?>[] { Scanner.class, PropertiesConfigurator.class, CommandLine.class, settings.getClass() },
                new Scanner(""), configuration, commandLine, settings);

        // Assert
        assertInstanceOf(GuidanceProcessor.class, processor);
        assertTrue(processor.getRootDir().equals(tempDir.toFile()));
    }

    @Test
    void createProcessor_buildsConfiguredActProcessorAndLoadsBundledAct() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        Object settings = settings("model", null);
        CommandLine commandLine = parse("--act", "help");

        // Act
        AIFileProcessor processor = (AIFileProcessor) invoke("createProcessor",
                new Class<?>[] { Scanner.class, PropertiesConfigurator.class, CommandLine.class, settings.getClass() },
                new Scanner(""), configuration, commandLine, settings);

        // Assert
        assertInstanceOf(ActProcessor.class, processor);
        ActProcessor actProcessor = (ActProcessor) processor;
        assertTrue(actProcessor.getActProperties().containsKey(ActProcessor.INPUTS_PROPERTY_NAME));
        assertTrue(actProcessor.isInteractive());
    }

    @Test
    void configureActsLocation_andDefaultAct_applyConfiguredValues() throws Exception {
        // Arrange
        Path acts = java.nio.file.Files.createDirectory(tempDir.resolve("acts"));
        java.nio.file.Files.write(acts.resolve("custom.toml"), java.util.Collections.singletonList("inputs = \"review\""));
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", configuration);
        CommandLine commandLine = parse("--acts", "acts", "--act", "custom");

        // Act
        invoke("configureActsLocation", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, ActProcessor.class },
                commandLine, configuration, processor);
        invoke("configureDefaultAct",
                new Class<?>[] { CommandLine.class, PropertiesConfigurator.class, Scanner.class, ActProcessor.class }, commandLine,
                configuration, new Scanner(""), processor);

        // Assert
        assertTrue(processor.getActProperties().containsKey(ActProcessor.INPUTS_PROPERTY_NAME));
    }

    private Object settings(String model, String instructions) throws Exception {
        Constructor<?> constructor = Class.forName("org.machanism.machai.gw.processor.Ghostwriter$RuntimeSettings")
                .getDeclaredConstructor();
        constructor.setAccessible(true);
        Object settings = constructor.newInstance();
        set(settings, "genai", model);
        set(settings, "instructions", instructions);
        set(settings, "projectDir", tempDir.toFile());
        set(settings, "paths", new String[] { "." });
        return settings;
    }

    private CommandLine parse(String... args) throws Exception {
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        return new DefaultParser().parse(options, args);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}
