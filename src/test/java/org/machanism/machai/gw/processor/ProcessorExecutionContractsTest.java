package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.ai.provider.AbstractAIProvider;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.machanism.machai.project.layout.ProjectLayout;

/** Verifies processor execution edge cases without invoking an AI provider. */
class ProcessorExecutionContractsTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void episodesRepeatRequestedWorkResolveNamedMovesAndExposeMetadata() {
        // Arrange
        ActProcessor processor = mock(ActProcessor.class);
        Episodes episodes = new Episodes(processor);
        episodes.setName("quality");
        episodes.setEpisodes(Arrays.asList("# First\nbody", "---\nkey: value\n---\n# Second\nbody"));
        AtomicInteger attempts = new AtomicInteger();

        // Act
        episodes.regularOrder(1, (id, prompt) -> {
            if (id == 1 && attempts.incrementAndGet() == 1) {
                throw new RepeatEpisodeException();
            }
            return "result-" + id;
        });

        // Assert
        assertEquals(2, attempts.get());
        verify(processor).addResults("result-1");
        verify(processor).addResults("result-2");
        assertEquals(2, episodes.getEpisodeId(1, new MoveToEpisodeException(null, "Second")));
        assertEquals(1, episodes.getEpisodeId(1, new MoveToEpisodeException(null, null)));
        List<?> metadata = (List<?>) episodes.getActInformation(2).get("EPISODES");
        assertEquals("Second", ((Map<?, ?>) metadata.get(1)).get("EPISODE_NAME"));
        assertThrows(EpisodeNotFoundException.class,
                () -> episodes.getEpisodeId(1, new MoveToEpisodeException(null, "missing")));
    }

    @Test
    void episodesValidateSelectionsAndRunOnlySelectedEpisodesInOrder() {
        // Arrange
        Episodes episodes = new Episodes(mock(ActProcessor.class));
        episodes.setEpisodes(Arrays.asList("one", "two", "three"));
        episodes.setSelectedEpisodes(Arrays.asList(3, 1));
        StringBuilder executed = new StringBuilder();

        // Act
        int lastEpisode = episodes.requestedOrder((id, prompt) -> {
            executed.append(id);
            return null;
        });

        // Assert
        assertEquals(1, lastEpisode);
        assertEquals("31", executed.toString());
        assertFalse(episodes.isRegularOrder());
        assertThrows(IllegalArgumentException.class, () -> episodes.setSelectedEpisodes(Collections.singletonList(0)));
        assertThrows(IllegalArgumentException.class, () -> episodes.setSelectedEpisodes(Collections.singletonList(4)));
    }

    @Test
    void scanDocumentsRejectsInvalidArgumentsAndParsePathHonorsDefaultPrompt() throws Exception {
        // Arrange
        TestableAIFileProcessor processor = new TestableAIFileProcessor(temporaryDirectory.toFile());
        File nested = Files.createDirectories(temporaryDirectory.resolve("docs")).toFile();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(null, "."));
        assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(temporaryDirectory.toFile(), " "));
        assertEquals("glob:docs{,/**}", processor.parsePath(temporaryDirectory.toFile(), "docs"));
        processor.defaultPrompt = "folder prompt";
        assertEquals("glob:docs", processor.parsePath(temporaryDirectory.toFile(), nested.getPath()));
        assertThrows(IllegalArgumentException.class,
                () -> processor.parsePath(temporaryDirectory.toFile(), temporaryDirectory.resolveSibling("outside").toString()));
    }

    @Test
    void cliHelpersResolveDefaultsAndPreserveMultilineInput() throws Exception {
        // Arrange
        Options options = (Options) invoke("createOptions", new Class<?>[0]);
        CommandLine commandLine = new DefaultParser().parse(options, new String[0]);
        PropertiesConfigurator configuration = new PropertiesConfigurator();
        String separator = AbstractAIProvider.LINE_SEPARATOR;

        // Act
        String[] paths = (String[]) invoke("resolvePaths", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class },
                commandLine, configuration);
        String entered = (String) invoke("promptForValue", new Class<?>[] { Scanner.class, String.class },
                new Scanner("first\\\nsecond\n"), "Input: ");
        StringBuilder buffer = new StringBuilder();
        invoke("appendContinuedLine", new Class<?>[] { StringBuilder.class, String.class }, buffer, "continued\\");

        // Assert
        assertEquals(new File(System.getProperty("user.dir")).getAbsoluteFile(),
                ((File) invoke("resolveProjectDir", new Class<?>[] { CommandLine.class, PropertiesConfigurator.class }, commandLine,
                        configuration)).getAbsoluteFile());
        assertEquals(".", paths[0]);
        assertEquals("first" + separator + "second", entered);
        assertEquals("continued" + separator, buffer.toString());
        assertTrue(Ghostwriter.USER_INPUT_PREFIX.startsWith(">"));
    }

    @Test
    void aiProcessorSupportsNonProviderLifecycleOperations() {
        // Arrange
        TestableAIFileProcessor processor = new TestableAIFileProcessor(temporaryDirectory.toFile());
        ProjectLayout layout = mock(ProjectLayout.class);
        FunctionTools tool = mock(FunctionTools.class);

        // Act
        processor.setInteractive(true);
        processor.setThreads(2);
        processor.processModulesMultiThreaded(layout, Collections.emptyList());
        processor.addTool(tool);
        String unavailableInput = processor.input();

        // Assert
        assertFalse(processor.isInteractive());
        assertEquals(null, unavailableInput);
        assertEquals(1, processor.functionTools.size());
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = Ghostwriter.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, arguments);
    }

    private static final class TestableAIFileProcessor extends AIFileProcessor {
        private String defaultPrompt;

        private TestableAIFileProcessor(File rootDir) {
            super(rootDir, new PropertiesConfigurator(), "model");
        }

        @Override
        protected String getDefaultPrompt() {
            return defaultPrompt;
        }
    }
}
