package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.manager.GenaiProviderManager;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.gw.tools.ProcessTerminationException;
import org.machanism.machai.project.layout.ProjectLayout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Covers interactive commands and otherwise isolated processor utility behavior. */
class ProcessorRemainingBehaviorTest {

    @TempDir
    Path tempDir;

    @Test
    void interactiveProcessing_handlesContinueDisableAndFollowUpCommands() {
        // Arrange
        ProjectLayout layout = layout();
        Genai provider = mock(Genai.class);
        when(provider.perform()).thenReturn("first", "second");
        InteractiveProcessor processor = new InteractiveProcessor(tempDir.toFile(), "model", "follow-up");
        processor.setInteractive(true);

        // Act
        String result;
        try (MockedStatic<GenaiProviderManager> providers = Mockito.mockStatic(GenaiProviderManager.class)) {
            providers.when(() -> GenaiProviderManager.getProvider(anyString(), any(LayeredConfigurator.class)))
                    .thenReturn(provider);
            result = processor.process(layout, tempDir.resolve("input.txt").toFile(), "prompt");
        }

        // Assert
        assertEquals("second", result);
        assertTrue(processor.isInteractive());
        verify(provider).prompt("follow-up");
        verify(provider, org.mockito.Mockito.times(2)).perform();
    }

    @Test
    void interactiveProcessing_exitCommandTerminatesSuccessfully() {
        // Arrange
        Genai provider = mock(Genai.class);
        when(provider.perform()).thenReturn("response");
        InteractiveProcessor processor = new InteractiveProcessor(tempDir.toFile(), "model", ".");
        processor.setInteractive(true);

        // Act + Assert
        try (MockedStatic<GenaiProviderManager> providers = Mockito.mockStatic(GenaiProviderManager.class)) {
            providers.when(() -> GenaiProviderManager.getProvider(anyString(), any(LayeredConfigurator.class)))
                    .thenReturn(provider);
            ProcessTerminationException exception = assertThrows(ProcessTerminationException.class,
                    () -> processor.process(layout(), tempDir.resolve("input.txt").toFile(), "prompt"));
            assertEquals(0, exception.getExitCode());
        }
    }

    @Test
    void actAutoToolHelpers_identifyAndParseSupportedMarkers() throws Exception {
        // Arrange
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());

        // Act + Assert
        assertTrue((Boolean) invoke(ActProcessor.class, processor, "isAutoToolSelection", new Class<?>[] { String.class }, "auto"));
        assertTrue((Boolean) invoke(ActProcessor.class, processor, "isAutoToolSelection", new Class<?>[] { String.class }, "{auto=local}"));
        assertFalse((Boolean) invoke(ActProcessor.class, processor, "isAutoToolSelection", new Class<?>[] { String.class }, "read_file"));
        assertEquals("", invoke(ActProcessor.class, processor, "getAutoToolSelectionQuery", new Class<?>[] { String.class }, "auto"));
        assertEquals("local only", invoke(ActProcessor.class, processor, "getAutoToolSelectionQuery", new Class<?>[] { String.class }, "{auto=local only}"));
    }

    @Test
    void actAutoToolCacheKey_usesActNameAndCurrentEpisode() throws Exception {
        // Arrange
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());
        java.lang.reflect.Field episodes = ActProcessor.class.getDeclaredField("episodes");
        episodes.setAccessible(true);
        ((Episodes) episodes.get(processor)).setName("review");
        String prefix = "The current act execution information: ";

        // Act
        String key = (String) invoke(ActProcessor.class, processor, "getInputId", new Class<?>[] { String[].class },
                (Object) new String[] { "instructions", prefix + "{\"CURRENT_EPISODE_ID\":2}", "prompt" });

        // Assert
        assertEquals("review#2", key);
    }

    @Test
    void ghostwriterInputAndResolvers_coverFallbacksAndContinuation() throws Exception {
        // Arrange
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        Options options = (Options) invokeStatic("createOptions", new Class<?>[0]);
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);

        // Act
        String value = (String) invokeStatic("promptForValue", new Class<?>[] { Scanner.class, String.class },
                new Scanner("first\\\nsecond\n"), "Value: ");
        String input = (String) invokeStatic("readActInput", new Class<?>[] { Scanner.class }, new Scanner("one\\\ntwo\n"));
        String[] paths = (String[]) invokeStatic("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);
        String[] excludes = (String[]) invokeStatic("resolveExcludes", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);

        // Assert
        assertEquals("first" + AbstractAIProvider.LINE_SEPARATOR + "second", value);
        assertEquals("one" + AbstractAIProvider.LINE_SEPARATOR + "two", input);
        assertArrayEquals(new String[] { "." }, paths);
        assertNull(excludes);
    }

    @Test
    void abstractProcessorUtilities_handleDefaultsAndInvalidSettings() throws Exception {
        // Arrange
        BasicProcessor processor = new BasicProcessor(tempDir.toFile());

        // Act + Assert
        processor.processParentFiles(layout());
        processor.processFile(layout(), tempDir.toFile());
        assertTrue(processor.listFiles(null).isEmpty());
        assertTrue(processor.shouldIncludeInListFiles(tempDir.toFile(), tempDir.resolve("outside").toFile()));
        assertFalse(processor.shouldExcludePath(null));
        assertEquals(0, AbstractFileProcessor.pathDepth("  "));
        assertThrows(IllegalArgumentException.class, () -> processor.setThreads(0));
        assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(0));
    }

    private ProjectLayout layout() {
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());
        when(layout.getParentId()).thenReturn(null);
        when(layout.getSources()).thenReturn(new ArrayList<String>());
        when(layout.getTests()).thenReturn(new ArrayList<String>());
        when(layout.getDocuments()).thenReturn(new ArrayList<String>());
        when(layout.getModules()).thenReturn(new ArrayList<String>());
        when(layout.getProjectName()).thenReturn("Demo");
        when(layout.getProjectId()).thenReturn("demo");
        when(layout.getProjectLayoutType()).thenReturn("test");
        return layout;
    }

    private static Object invoke(Class<?> declaringClass, Object target, String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = declaringClass.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(target, arguments);
    }

    private static Object invokeStatic(String name, Class<?>[] types, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static final class InteractiveProcessor extends AIFileProcessor {
        private final java.util.List<String> inputs;
        private int index;

        private InteractiveProcessor(File rootDir, String model, String... inputs) {
            super(rootDir, new PropertiesConfigurator(), model);
            this.inputs = Arrays.asList(inputs);
        }

        @Override
        protected String input() {
            return index < inputs.size() ? inputs.get(index++) : null;
        }
    }

    private static final class BasicProcessor extends AbstractFileProcessor {
        private BasicProcessor(File rootDir) {
            super(rootDir, new PropertiesConfigurator());
        }
    }
}
