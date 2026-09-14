package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional contract, boundary, and I/O tests shared by reviewer implementations. */
class ReviewerContractAndFailureTest {

    @TempDir
    Path tempDir;

    @Test
    void reviewersReturnFreshExtensionArraysWithoutCrossInstanceMutation() {
        Reviewer[] reviewers = { new HtmlReviewer(), new JavaReviewer(), new MarkdownReviewer(), new PumlReviewer(),
                new PythonReviewer(), new TextReviewer(), new TypeScriptReviewer() };
        String[][] expected = { { "html", "htm", "xml" }, { "java" }, { "md" }, { "puml" }, { "py" },
                { "txt" }, { "ts" } };

        for (int i = 0; i < reviewers.length; i++) {
            String[] first = reviewers[i].getSupportedFileExtensions();
            first[0] = "mutated";
            assertArrayEquals(expected[i], reviewers[i].getSupportedFileExtensions());
        }
    }

    @Test
    void htmlReviewer_returnsNullForCommentWithoutGuidanceTag() throws IOException {
        Path file = write("page.html", "<!-- ordinary comment -->\n<body>content</body>");

        String result = new HtmlReviewer().perform(tempDir.toFile(), file.toFile());

        assertNull(result);
    }

    @Test
    void markdownReviewer_returnsNullForTextMentionOutsideComment() throws IOException {
        Path file = write("readme.md", "The text @guidance is not a comment.");

        String result = new MarkdownReviewer().perform(tempDir.toFile(), file.toFile());

        assertNull(result);
    }

    @Test
    void pumlReviewer_rejectsNullFileBeforeAttemptingI0() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new PumlReviewer().perform(tempDir.toFile(), null));

        assertTrue(exception.getMessage().contains("guidancesFile"));
    }

    @Test
    void textReviewer_onlyProcessesExactGuidanceFileName() throws IOException {
        Path file = write("guidance.txt", "instructions");

        String result = new TextReviewer().perform(tempDir.toFile(), file.toFile());

        assertNull(result);
    }

    @Test
    void reviewers_propagateMissingFileAsIOException() {
        File missing = tempDir.resolve("missing.ts").toFile();

        assertThrows(IOException.class, () -> new TypeScriptReviewer().perform(tempDir.toFile(), missing));
        assertThrows(IOException.class, () -> new PythonReviewer().perform(tempDir.toFile(), missing));
        assertThrows(IOException.class, () -> new MarkdownReviewer().perform(tempDir.toFile(), missing));
    }

    private Path write(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
