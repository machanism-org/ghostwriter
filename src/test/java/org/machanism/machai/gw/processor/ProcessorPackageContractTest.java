package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.DefaultProjectLayout;
import org.machanism.machai.project.layout.ProjectLayout;
import org.tomlj.Toml;

/**
 * Package-level contract tests for filesystem traversal and data conversion
 * paths that do not require a live AI provider.
 */
class ProcessorPackageContractTest {

    @TempDir
    Path tempDir;

    @Test
    void abstractProcessor_settersExposeConfiguredStateAndValidateThreads() {
        // Arrange
        RecordingProcessor processor = new RecordingProcessor(tempDir.toFile());

        // Act
        processor.setThreads(3);
        processor.setNonRecursive(true);
        processor.setExcludes(new String[] { "target" });
        processor.setPath(tempDir.toFile());
        processor.setPathMatcher(path -> true);
        processor.setModuleThreadTimeoutMinutes(2);

        // Assert
        assertEquals(3, processor.threadsValue());
        assertTrue(processor.isNonRecursive());
        assertEquals("target", processor.getExcludes()[0]);
        assertEquals(tempDir.toFile(), processor.getPath());
        assertNotNull(processor.getPathMatcher());
        assertEquals(2, processor.getModuleThreadTimeoutMinutes());
        assertThrows(IllegalArgumentException.class, () -> processor.setThreads(0));
    }

    @Test
    void listAndProcessProjectDirHonorsPatternsAndExclusions() throws Exception {
        // Arrange
        Path source = Files.createDirectories(tempDir.resolve("src"));
        Files.write(source.resolve("keep.txt"), "keep".getBytes("UTF-8"));
        Files.write(source.resolve("skip.txt"), "skip".getBytes("UTF-8"));
        RecordingProcessor processor = new RecordingProcessor(tempDir.toFile());
        processor.setExcludes(new String[] { "skip.txt" });
        ProjectLayout layout = new DefaultProjectLayout().projectDir(tempDir.toFile());

        // Act
        processor.processProjectDir(layout, "glob:**/*.txt");

        // Assert
        assertEquals(1, processor.processed.size());
        assertEquals("keep.txt", processor.processed.get(0).getName());
        assertTrue(processor.shouldIncludeInListFiles(tempDir.toFile(), source.toFile()));
        assertFalse(processor.shouldIncludeInListFiles(source.toFile(), source.resolve("skip.txt").toFile()));
    }

    @Test
    void aiProcessor_parsePathAndProcessInfoDescribeProjectRelativeExecution() {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");
        File child = tempDir.resolve("src").toFile();

        // Act
        String pattern = processor.parsePath(tempDir.toFile(), "src");
        String info = processor.getProcessInfo(new DefaultProjectLayout().projectDir(tempDir.toFile()), child);

        // Assert
        assertEquals("glob:src{,/**}", pattern);
        assertTrue(info.contains("PROCESSED_FILE_REL_PATH: src"));
        assertTrue(info.contains("OS_NAME:"));
        assertTrue(info.contains("not-interactive"));
        assertThrows(IllegalArgumentException.class,
                () -> processor.parsePath(tempDir.toFile(), tempDir.resolveSibling("outside").toString()));
    }

    @Test
    void actDataConversionMergesInheritedValuesAndTomlScalars() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        properties.put("instructions", "before ${super.value} after");
        properties.put("inputs", java.util.Arrays.asList("parent ${super.value}"));
        String toml = "instructions = \"child\"\ninputs = [\"episode\"]\nactive = true\ncount = 2\nratio = 1.5\n";

        // Act
        ActProcessor.setActData(properties, Toml.parse(toml));

        // Assert
        assertEquals("before child after", properties.get("instructions"));
        assertEquals(java.util.Arrays.asList("parent episode"), properties.get("inputs"));
        assertEquals("true", properties.get("active"));
        assertEquals("2", properties.get("count"));
        assertEquals("1.5", properties.get("ratio"));
    }

    @Test
    void actClasspathLoaderRejectsUnsafeNamesBeforeResourceAccess() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> ActProcessor.tryLoadActFromClasspath(properties, "../outside"));
        assertThrows(IllegalArgumentException.class,
                () -> ActProcessor.tryLoadActFromClasspath(properties, "nested\\outside"));
    }

    private static final class RecordingProcessor extends AbstractFileProcessor {
        private final List<File> processed = new ArrayList<>();

        private RecordingProcessor(File rootDir) {
            super(rootDir, new PropertiesConfigurator());
        }

        private int threadsValue() {
            return getThreadsForTest();
        }

        private int getThreadsForTest() {
            try {
                java.lang.reflect.Field field = AbstractFileProcessor.class.getDeclaredField("threads");
                field.setAccessible(true);
                return field.getInt(this);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        }

        @Override
        protected void processFile(ProjectLayout projectLayout, File file) {
            processed.add(file);
        }

        @Override
        protected void processParentFiles(ProjectLayout projectLayout) {
            // Not part of these traversal contracts.
        }
    }
}
