package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.project.layout.ProjectLayout;

/** Tests module-path eligibility without invoking filesystem layout discovery. */
class GuidanceProcessorModuleTest {

    @TempDir
    Path tempDir;

    @Test
    void processModule_withoutConfiguredPath_delegatesToModuleScanner() throws IOException {
        // Arrange
        ModuleRecordingProcessor processor = new ModuleRecordingProcessor(tempDir.toFile(), layout(tempDir.toFile()));

        // Act
        processor.processConfiguredModule(tempDir.toFile(), "module");

        // Assert
        assertTrue(processor.scannedModule);
    }

    @Test
    void processModule_withPathInsideModule_delegatesToModuleScanner() throws IOException {
        // Arrange
        File moduleDir = tempDir.resolve("module").toFile();
        File nestedPath = tempDir.resolve("module/nested/input.java").toFile();
        ModuleRecordingProcessor processor = new ModuleRecordingProcessor(moduleDir, layout(moduleDir));
        processor.setPath(nestedPath);

        // Act
        processor.processConfiguredModule(tempDir.toFile(), "module");

        // Assert
        assertTrue(processor.scannedModule);
    }

    private static ProjectLayout layout(File projectDir) {
        ProjectLayout layout = mock(ProjectLayout.class);
        when(layout.getProjectDir()).thenReturn(projectDir);
        when(layout.isExcludedPath(org.mockito.ArgumentMatchers.any(File.class))).thenReturn(false);
        return layout;
    }

    private static final class ModuleRecordingProcessor extends GuidanceProcessor {
        private final ProjectLayout moduleLayout;
        private boolean scannedModule;

        private ModuleRecordingProcessor(File rootDir, ProjectLayout moduleLayout) {
            super(rootDir, "model", new PropertiesConfigurator());
            this.moduleLayout = moduleLayout;
        }

        @Override
        void loadReviewers() {
            // Avoid service discovery; reviewer behavior is irrelevant to module routing.
        }

        @Override
        public ProjectLayout getProjectLayout(File projectDir) {
            return moduleLayout;
        }

        @Override
        public void scanFolder(File projectDir) {
            scannedModule = true;
        }

        private void processConfiguredModule(File projectDir, String module) throws IOException {
            super.processModule(projectDir, module);
        }
    }
}
