package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.machanism.machai.process.provider.ProcessProvider;

/** Verifies automatic Act tool selection and its per-episode cache. */
class ActProcessorAutoToolSelectionTest {

    @TempDir
    Path tempDir;

    @Test
    void automaticToolMarkerHelpers_recognizePlainAndMappedValuesOnly() throws Exception {
        // Arrange
        ActProcessor processor = new ActProcessor(tempDir.toFile(), "model", new PropertiesConfigurator());

        // Act + Assert
        assertEquals(true, invokeBoolean(processor, "isAutoToolSelection", "auto"));
        assertEquals(true, invokeBoolean(processor, "isAutoToolSelection", "{auto=local only}"));
        assertEquals(false, invokeBoolean(processor, "isAutoToolSelection", "read_file"));
        assertEquals("", invokeString(processor, "getAutoToolSelectionQuery", "auto"));
        assertEquals("local only", invokeString(processor, "getAutoToolSelectionQuery", "{auto=local only}"));
    }

    private static void invokeApplyTools(ActProcessor processor, ProcessProvider provider, String[] prompts) throws Exception {
        Method method = ActProcessor.class.getDeclaredMethod("applyTools", String.class, String[].class, ProcessProvider.class,
                String[].class);
        method.setAccessible(true);
        method.invoke(processor, "system instructions", prompts, provider, new String[] { "auto" });
    }

    private static boolean invokeBoolean(ActProcessor processor, String name, String value) throws Exception {
        Method method = ActProcessor.class.getDeclaredMethod(name, String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(processor, value);
    }

    private static String invokeString(ActProcessor processor, String name, String value) throws Exception {
        Method method = ActProcessor.class.getDeclaredMethod(name, String.class);
        method.setAccessible(true);
        return (String) method.invoke(processor, value);
    }

    @SuppressWarnings("unchecked")
    private static String[] cachedTools(ActProcessor processor, String key) throws Exception {
        Field field = ActProcessor.class.getDeclaredField("autoToolsMap");
        field.setAccessible(true);
        return ((java.util.Map<String, String[]>) field.get(processor)).get(key);
    }

    private static Episodes episodes(ActProcessor processor) throws Exception {
        Field field = ActProcessor.class.getDeclaredField("episodes");
        field.setAccessible(true);
        return (Episodes) field.get(processor);
    }
}
