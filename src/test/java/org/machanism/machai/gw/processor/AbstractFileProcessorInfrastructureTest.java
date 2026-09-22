package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

/** Exercises module-worker execution and file inclusion decisions in the shared processor. */
class AbstractFileProcessorInfrastructureTest {

    @TempDir
    Path tempDir;

    @Test
    void processModulesMultiThreaded_processesEveryModuleAndPropagatesIoFailures() {
        // Arrange
        RecordingProcessor processor = new RecordingProcessor(tempDir.toFile());
        processor.setThreads(2);
        ProjectLayout layout = layout(tempDir.toFile());

        // Act
        processor.processModulesMultiThreaded(layout, Arrays.asList("one", "two"));

        // Assert
        assertEquals(2, processor.moduleCalls.get());
        processor.failWithIo = true;
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> processor.processModulesMultiThreaded(layout, Collections.singletonList("broken")));
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void match_honorsNullExcludedExactAndPatternBasedCandidates() throws Exception {
        // Arrange
        File projectDir = tempDir.resolve("project").toFile();
        assertTrue(projectDir.mkdirs());
        File textFile = new File(projectDir, "notes.txt");
        Files.write(textFile.toPath(), Collections.singletonList("content"));
        RecordingProcessor processor = new RecordingProcessor(tempDir.toFile());
        ProjectLayout layout = layout(projectDir);

        // Act + Assert
        assertFalse(processor.matches(null, layout));
        when(layout.isExcludedPath(textFile)).thenReturn(true);
        assertFalse(processor.matches(textFile, layout));
        when(layout.isExcludedPath(textFile)).thenReturn(false);
        processor.setPath(textFile);
        assertTrue(processor.matches(textFile, layout));
        processor.setPathMatcher(FileSystems.getDefault().getPathMatcher("glob:**.txt"));
        assertTrue(processor.matches(textFile, layout));
        processor.setPath(new File(projectDir, "other"));
        assertFalse(processor.matches(textFile, layout));
    }

    @Test
    void shutdownExecutor_acceptsNullAndIncludeFilteringRejectsOutsideFile() {
        // Arrange
        RecordingProcessor processor = new RecordingProcessor(tempDir.toFile());
        File outside = tempDir.resolveSibling("outside.txt").toFile();

        // Act + Assert
        processor.shutdownExecutor(null);
        assertFalse(processor.shouldIncludeInListFiles(tempDir.toFile(), outside));
    }

    private static ProjectLayout layout(File projectDir) {
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(projectDir);
        when(layout.isExcludedPath(org.mockito.ArgumentMatchers.any(File.class))).thenReturn(false);
        return layout;
    }

    private static final class RecordingProcessor extends AbstractFileProcessor {
        private final AtomicInteger moduleCalls = new AtomicInteger();
        private boolean failWithIo;

        private RecordingProcessor(File rootDir) {
            super(rootDir, new PropertiesConfigurator());
        }

        @Override
        protected void processModule(File projectDir, String module) throws IOException {
            moduleCalls.incrementAndGet();
            if (failWithIo) {
                throw new IOException(module);
            }
        }

        private boolean matches(File file, ProjectLayout layout) {
            return match(file, layout);
        }

        @Override
        protected void processFile(ProjectLayout projectLayout, File file) {
            // Not needed by these traversal-focused tests.
        }
    }
}
