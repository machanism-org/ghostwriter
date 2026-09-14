package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.PropertiesConfigurator;
import org.tomlj.Toml;

/** Exercises act-definition merging and processor boundary behavior without a provider. */
class ProcessorWorkflowCompletionTest {

    @TempDir
    Path tempDir;

    @Test
    void setAct_expandsTaskShorthandAndLoadsBundledTaskDefinition() throws Exception {
        // Arrange
        ActProcessor processor = processor("configured:model");

        // Act
        processor.setAct("> write release notes");

        // Assert
        assertEquals("write release notes", processor.getActProperties().get(ActProcessor.PUBLIC_USER_PROMPT_PROP_NAME));
        assertFalse(processor.getDefaultPrompt().isEmpty());
    }

    @Test
    void setAct_withoutArgumentsUsesDefaultPromptAndSupportsEpisodeStopSelection() throws Exception {
        // Arrange
        Path acts = Files.createDirectory(tempDir.resolve("acts"));
        Files.write(acts.resolve("demo.toml"), java.util.Arrays.asList(
                "inputs = [\"first\", \"second\"]",
                "[default]",
                "public.prompt = \"fallback prompt\""), StandardCharsets.UTF_8);
        ActProcessor processor = processor("model");
        processor.setActsLocation("acts");

        // Act
        processor.setAct("demo#2!");

        // Assert
        assertEquals("fallback prompt", processor.getActProperties().get(ActProcessor.PUBLIC_USER_PROMPT_PROP_NAME));
        assertEquals("first", processor.getDefaultPrompt());
        assertEquals(2, processor.getActProperties().containsKey(ActProcessor.INPUTS_PROPERTY_NAME)
                ? ((List<?>) processor.getActProperties().get(ActProcessor.INPUTS_PROPERTY_NAME)).size() : 0);
        assertTrue(readBoolean(processor, "disableNormalOrder"));
    }

    @Test
    void loadAct_inheritsParentAndMergesStringAndEpisodeValues() throws Exception {
        // Arrange
        Path acts = Files.createDirectory(tempDir.resolve("inherited"));
        Files.write(acts.resolve("base.toml"), java.util.Arrays.asList(
                "instructions = \"base instructions\"",
                "inputs = [\"base one\", \"base two\"]",
                "label = \"base\""), StandardCharsets.UTF_8);
        Files.write(acts.resolve("child.toml"), java.util.Arrays.asList(
                "basedOn = \"base\"",
                "instructions = \"before ${super.value} after\"",
                "inputs = [\"child ${super.value}\", \"replacement\"]",
                "label = \"child ${super.value}\""), StandardCharsets.UTF_8);
        Map<String, Object> properties = new HashMap<>();

        // Act
        ActProcessor.loadAct("child", properties, acts.toString(), tempDir.toFile());

        // Assert
        assertEquals("before base instructions after", properties.get(ActProcessor.INSTRUCTIONS_PROPERTY_NAME));
        assertEquals("child base one", ((List<?>) properties.get(ActProcessor.INPUTS_PROPERTY_NAME)).get(0));
        assertEquals("replacement", ((List<?>) properties.get(ActProcessor.INPUTS_PROPERTY_NAME)).get(1));
        assertEquals("child base", properties.get("label"));
        assertFalse(properties.containsKey(ActProcessor.BASED_ON_PROPERTY_NAME));
    }

    @Test
    void setActData_convertsPrimitiveTomlValuesAndMergesArrayWithInheritedString() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();
        properties.put("inputs", "parent ${super.value}");

        // Act
        ActProcessor.setActData(properties, Toml.parse("enabled = true\nratio = 1.5\ninputs = [\"child\"]"));

        // Assert
        assertEquals("true", properties.get("enabled"));
        assertEquals("1.5", properties.get("ratio"));
        assertEquals("parent child", ((List<?>) properties.get("inputs")).get(0));
    }

    @Test
    void scanDocuments_rejectsInvalidArgumentsAndBuildsExpectedPaths() {
        // Arrange
        AIFileProcessor processor = new AIFileProcessor(tempDir.toFile(), new PropertiesConfigurator(), "model");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(null, "."));
        assertThrows(IllegalArgumentException.class, () -> processor.scanDocuments(tempDir.toFile(), " "));
        assertEquals("glob:src{,/**}", processor.parsePath(tempDir.toFile(), "src"));
        assertEquals(tempDir.resolve("src").toFile(), processor.getPath());
        processor.setDefaultPrompt("folder prompt");
        assertEquals("glob:src", processor.parsePath(tempDir.toFile(), "src"));
        assertThrows(IllegalArgumentException.class,
                () -> processor.parsePath(tempDir.toFile(), tempDir.getParent().resolve("outside").toString()));
    }

    @Test
    void helperMethods_handleEmptyAndSpecialInputSafely() {
        // Arrange
        AbstractFileProcessor processor = new AbstractFileProcessorTest.TestProcessor(tempDir.toFile(),
                new PropertiesConfigurator());
        processor.setExcludes(new String[] { null, "glob:**/*.tmp", "exact.txt" });

        // Act + Assert
        assertTrue(processor.shouldExcludePath(new File("dir/value.tmp").toPath()));
        assertTrue(processor.shouldExcludePath(new File("exact.txt").toPath()));
        assertFalse(processor.shouldExcludePath(new File("keep.java").toPath()));
        assertEquals(0, AbstractFileProcessor.pathDepth("  "));
        assertTrue(AbstractFileProcessor.isPathPattern("regex:.*"));
        assertFalse(AbstractFileProcessor.isPathPattern("src"));
        assertEquals("ordinary prompt", AIFileProcessor.removeFrontMatterData("  ordinary prompt  "));
    }

    @Test
    void episodes_handlesEmptySelectionAndNullMoveDestination() {
        // Arrange
        Episodes episodes = new Episodes(processor("model"));
        episodes.setEpisodes(java.util.Collections.singletonList("# One\ntext"));

        // Act
        int last = episodes.requestedOrder((id, prompt) -> "unused");

        // Assert
        assertEquals(0, last);
        assertEquals(5, episodes.getEpisodeId(5, new org.machanism.machai.gw.tools.MoveToEpisodeException(null, null)));
        assertThrows(NullPointerException.class, () -> episodes.setSelectedEpisodes(null));
    }

    private ActProcessor processor(String model) {
        return new ActProcessor(tempDir.toFile(), model, new PropertiesConfigurator());
    }

    private static boolean readBoolean(Object target, String fieldName) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getBoolean(target);
    }
}
