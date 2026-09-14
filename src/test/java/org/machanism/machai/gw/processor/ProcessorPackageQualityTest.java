package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * Focused contract tests for package-level processor behavior that does not
 * require a live GenAI provider.
 */
class ProcessorPackageQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void aiFileProcessor_processFolderUsesConfiguredDefaultPrompt() {
        // Arrange
        FolderProcessor processor = new FolderProcessor(tempDir.toFile());
        processor.setDefault("summarize");
        ProjectLayout layout = layout(tempDir.toFile());

        // Act
        processor.processFolder(layout);

        // Assert
        assertEquals(tempDir.toFile(), processor.processedFile);
        assertEquals("summarize", processor.processedPrompt);
    }

    @Test
    void aiFileProcessor_processFolderWrapsProcessingFailures() {
        // Arrange
        FolderProcessor processor = new FolderProcessor(tempDir.toFile());
        processor.failure = new IllegalStateException("provider unavailable");

        // Act + Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.processFolder(layout(tempDir.toFile())));
        assertNotNull(exception.getCause());
        assertEquals("provider unavailable", exception.getCause().getMessage());
    }

    @Test
    void abstractFileProcessor_rejectsNonPositiveShutdownTimeout() {
        // Arrange
        FolderProcessor processor = new FolderProcessor(tempDir.toFile());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(0));
        assertThrows(IllegalArgumentException.class, () -> processor.setModuleThreadTimeoutMinutes(-1));
        assertEquals(60, processor.getModuleThreadTimeoutMinutes());
    }

    private static ProjectLayout layout(File projectDir) {
        ProjectLayout layout = org.mockito.Mockito.mock(ProjectLayout.class);
        org.mockito.Mockito.when(layout.getProjectDir()).thenReturn(projectDir);
        org.mockito.Mockito.when(layout.getParentId()).thenReturn(null);
        org.mockito.Mockito.when(layout.getSources()).thenReturn(Collections.emptyList());
        org.mockito.Mockito.when(layout.getTests()).thenReturn(Collections.emptyList());
        org.mockito.Mockito.when(layout.getDocuments()).thenReturn(Collections.emptyList());
        org.mockito.Mockito.when(layout.getModules()).thenReturn(Collections.emptyList());
        org.mockito.Mockito.when(layout.getProjectName()).thenReturn("test");
        org.mockito.Mockito.when(layout.getProjectId()).thenReturn("test");
        org.mockito.Mockito.when(layout.getProjectLayoutType()).thenReturn("test");
        return layout;
    }

    private static final class FolderProcessor extends AIFileProcessor {
        private File processedFile;
        private String processedPrompt;
        private RuntimeException failure;

        private FolderProcessor(File rootDir) {
            super(rootDir, new PropertiesConfigurator(), "test-model");
        }

        private void setDefault(String prompt) {
            setDefaultPrompt(prompt);
        }

        @Override
        protected String process(ProjectLayout projectLayout, File file, String instructions, String... prompts) {
            if (failure != null) {
                throw failure;
            }
            processedFile = file;
            processedPrompt = prompts.length == 0 ? null : prompts[prompts.length - 1];
            return "done";
        }
    }
}
