package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Covers file-system failure paths for each reviewer that reads source content. */
class ReviewerMissingInputCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void htmlReviewer_propagatesIOExceptionForMissingFile() {
        // Arrange
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.html").toFile();

        // Act and assert
        assertThrows(IOException.class, () -> new HtmlReviewer().perform(project, missing));
    }

    @Test
    void javaReviewer_propagatesIOExceptionForMissingFile() {
        // Arrange
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.java").toFile();

        // Act and assert
        assertThrows(IOException.class, () -> new JavaReviewer().perform(project, missing));
    }

    @Test
    void pumlReviewer_propagatesIOExceptionForMissingFile() {
        // Arrange
        File project = tempDir.toFile();
        File missing = tempDir.resolve("missing.puml").toFile();

        // Act and assert
        assertThrows(IOException.class, () -> new PumlReviewer().perform(project, missing));
    }

    @Test
    void textReviewer_propagatesIOExceptionForMissingGuidanceFile() {
        // Arrange
        File project = tempDir.toFile();
        File missing = tempDir.resolve("@guidance.txt").toFile();

        // Act and assert
        assertThrows(IOException.class, () -> new TextReviewer().perform(project, missing));
    }

    @Test
    void textReviewer_readsUtf8GuidanceWithoutChangingUnicodeContent() throws IOException {
        // Arrange
        Path file = tempDir.resolve("@guidance.txt");
        String content = "Résumé — 規則";
        Files.write(file, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act
        String prompt = new TextReviewer().perform(tempDir.toFile(), file.toFile());

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(prompt.contains(content));
    }
}
