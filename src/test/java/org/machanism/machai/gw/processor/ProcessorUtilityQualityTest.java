package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

/** Focused boundary tests for the package's shared, non-AI utility behavior. */
class ProcessorUtilityQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void pathPatternAndDepthHelpersHandleBlankCaseAndSeparators() {
        // Arrange
        String windowsPath = "one\\two\\file.txt";

        // Act / Assert
        assertTrue(AbstractFileProcessor.isPathPattern("GLOB:**.java"));
        assertTrue(AbstractFileProcessor.isPathPattern("regex:.*\\\\.java"));
        assertFalse(AbstractFileProcessor.isPathPattern("src/main"));
        assertNull(AbstractFileProcessor.getPatternPath(null));
        assertNull(AbstractFileProcessor.getPatternPath(" "));
        assertEquals(0, AbstractFileProcessor.pathDepth(""));
        assertEquals(3, AbstractFileProcessor.pathDepth(windowsPath));
        assertEquals(2, AbstractFileProcessor.pathDepth("one/two"));
    }

    @Test
    void processorConfigurationSettersExposeValuesAndRejectInvalidValues() {
        // Arrange
        TestProcessor processor = new TestProcessor(tempDir.toFile());

        // Act
        processor.setThreads(3);
        processor.setNonRecursive(true);
        processor.setModuleThreadTimeoutMinutes(2);
        File scanPath = tempDir.resolve("scan").toFile();
        processor.setPath(scanPath);
        processor.setExcludes(new String[] {"target", "glob:**.tmp"});

        // Assert
        assertTrue(processor.isNonRecursive());
        assertEquals(2, processor.getModuleThreadTimeoutMinutes());
        assertSame(scanPath, processor.getPath());
        assertEquals(2, processor.getExcludes().length);
        assertThrows(IllegalArgumentException.class, () -> processor.setThreads(0));
        assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(0));
    }

    @Test
    void exclusionRulesSupportExactNamesPatternsNullEntriesAndCaseSensitivity() {
        // Arrange
        TestProcessor processor = new TestProcessor(tempDir.toFile());
        processor.setExcludes(new String[] {null, "target", "glob:**/*.tmp"});

        // Act / Assert
        assertFalse(processor.shouldExcludePath(null));
        assertTrue(processor.shouldExcludePath(new File("target").toPath()));
        assertTrue(processor.shouldExcludePath(new File("nested/cache.tmp").toPath()));
        assertFalse(processor.shouldExcludePath(new File("Target").toPath()));
        processor.setExcludes(null);
        assertFalse(processor.shouldExcludePath(new File("target").toPath()));
    }

    @Test
    void listAndProcessFolderTraverseFilesAndWrapListingFailures() throws Exception {
        // Arrange
        Path nested = Files.createDirectories(tempDir.resolve("nested"));
        Path file = Files.write(nested.resolve("sample.txt"), Collections.singletonList("sample"));
        TestProcessor processor = new TestProcessor(tempDir.toFile());
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(tempDir.toFile());
        when(layout.isExcludedPath(org.mockito.ArgumentMatchers.any(File.class))).thenReturn(false);

        // Act
        List<File> listed = processor.list(tempDir.toFile());
        processor.processFolder(layout);

        // Assert
        assertTrue(listed.contains(file.toFile()));
        assertTrue(processor.processed.contains(file.toFile()));
        assertTrue(processor.list(tempDir.resolve("missing").toFile()).isEmpty());
        assertTrue(processor.processed.size() >= 1);
    }

    @Test
    void episodeExceptionPreservesNullMessageAndConstantsRemainStable() {
        // Arrange / Act
        EpisodeNotFoundException exception = new EpisodeNotFoundException(null);

        // Assert
        assertNull(exception.getMessage());
        assertEquals(73, GWConstants.LOG_LINE_LENGTH);
        assertEquals('\\', GWConstants.MULTIPLE_LINES_BREAKER);
    }

    private static final class TestProcessor extends AbstractFileProcessor {
        private final List<File> processed = new java.util.ArrayList<>();

        private TestProcessor(File root) {
            super(root, new PropertiesConfigurator());
        }

        private List<File> list(File directory) throws IOException {
            return listFiles(directory);
        }

        @Override
        protected void processFile(ProjectLayout layout, File file) {
            processed.add(file);
        }
    }
}
