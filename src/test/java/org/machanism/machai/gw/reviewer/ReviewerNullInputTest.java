package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests explicit null-input behavior at reviewer API boundaries. */
class ReviewerNullInputTest {

    @TempDir
    Path tempDir;

    @Test
    void pumlReviewer_perform_rejectsNullGuidanceFileAfterValidatingProjectDirectory() {
        // Arrange
        PumlReviewer reviewer = new PumlReviewer();
        File projectDirectory = tempDir.toFile();

        // Act + Assert
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> reviewer.perform(projectDirectory, null));
        org.junit.jupiter.api.Assertions.assertEquals("guidancesFile must not be null", exception.getMessage());
    }

    @Test
    void textReviewer_getPrompt_returnsNullWhenRawGuidanceIsNull() {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        File projectDirectory = tempDir.toFile();
        File guidanceFile = tempDir.resolve("@guidance.txt").toFile();

        // Act
        String result = reviewer.getPrompt(projectDirectory, guidanceFile, null);

        // Assert
        assertNull(result);
    }
}
