package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

class ProcessorValueTypesTest {

    @Test
    void projectContextKeysExposeTheirStableRegistryValues() {
        // Arrange
        ProjectContextKey[] keys = ProjectContextKey.values();

        // Act and Assert
        assertEquals("OPERATING_SYSTEM", ProjectContextKey.OPERATING_SYSTEM.getKey());
        assertEquals("PROJECT_NAME", ProjectContextKey.PROJECT_NAME.getKey());
        assertEquals("PROJECT_ID", ProjectContextKey.PROJECT_ID.getKey());
        assertEquals("PROJECT_DIR_NAME", ProjectContextKey.PROJECT_DIR_NAME.getKey());
        assertEquals("PARENT_PROJECT_ID", ProjectContextKey.PARENT_PROJECT_ID.getKey());
        assertEquals("PARENT_PROJECT_DIR_NAME", ProjectContextKey.PARENT_PROJECT_DIR_NAME.getKey());
        assertEquals("REL_PATH_FROM_ROOT", ProjectContextKey.REL_PATH_FROM_ROOT.getKey());
        assertEquals("LAYOUT_TYPE", ProjectContextKey.LAYOUT_TYPE.getKey());
        assertEquals("SRC_AND_RESOURCE_DIRS", ProjectContextKey.SRC_AND_RESOURCE_DIRS.getKey());
        assertEquals("TEST_SRC_AND_RESOURCE_DIRS", ProjectContextKey.TEST_SRC_AND_RESOURCE_DIRS.getKey());
        assertEquals("DOCS_DIRS", ProjectContextKey.DOCS_DIRS.getKey());
        assertEquals("MODULES", ProjectContextKey.MODULES.getKey());
        assertEquals(12, keys.length);
    }

    @Test
    void episodeNotFoundExceptionRetainsTheUnresolvedEpisodeName() {
        // Arrange
        String episodeName = "missing-episode";

        // Act
        EpisodeNotFoundException exception = new EpisodeNotFoundException(episodeName);

        // Assert
        assertEquals(episodeName, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void constantsDefineTheExpectedConfigurationContract() {
        // Arrange and Act
        String projectDirectoryProperty = GWConstants.PROJECT_DIR_PROP_NAME;

        // Assert
        assertEquals("project.dir", projectDirectoryProperty);
        assertEquals("gw.properties", GWConstants.GW_CONFIG_FILE_NAME);
        assertEquals("gw.config", GWConstants.CONFIG_PROP_NAME);
        assertEquals("gw.model", GWConstants.MODEL_PROP_NAME);
        assertEquals("gw.instructions", GWConstants.INSTRUCTIONS_PROP_NAME);
        assertEquals("gw.excludes", GWConstants.EXCLUDES_PROP_NAME);
        assertEquals("gw.acts", GWConstants.ACTS_LOCATION_PROP_NAME);
        assertEquals("gw.act", GWConstants.ACT_PROP_NAME);
        assertEquals("gw.threads", GWConstants.THREADS_PROP_NAME);
        assertEquals("gw.path", GWConstants.PATH_PROP_NAME);
        assertEquals("gw.nonRecursive", GWConstants.NONRECURSIVE_PROP_NAME);
        assertEquals("gw.interactive", GWConstants.INTERACTIVE_MODE_PROP_NAME);
        assertEquals('\\', GWConstants.MULTIPLE_LINES_BREAKER);
        assertEquals(73, GWConstants.LOG_LINE_LENGTH);
    }

    @Test
    void constantsUtilityConstructorIsPrivateButCanBeCoveredReflectively() throws Exception {
        // Arrange
        Constructor<GWConstants> constructor = GWConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act
        GWConstants instance = constructor.newInstance();

        // Assert
        assertTrue(instance != null);
    }
}
