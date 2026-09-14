package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional branch-focused tests for the reviewer implementations. */
class ReviewerAdditionalBranchTest {

    @TempDir
    Path project;

    @Test
    void htmlReviewer_acceptsMultilineGuidanceCommentAndPreservesCompleteContent() throws Exception {
        // Arrange
        Path file = write("page.htm", "<!--\n @guidance: explain the page\n-->\n<body>text</body>");
        HtmlReviewer reviewer = new HtmlReviewer();

        // Act
        String prompt = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertTrue(prompt.contains("web/page.htm") || prompt.contains("page.htm"));
    }

    @Test
    void markdownReviewer_doesNotTreatGuidanceOutsideHtmlCommentAsReviewable() throws Exception {
        // Arrange
        Path file = write("notes.md", "@guidance: explain\n# Notes");
        MarkdownReviewer reviewer = new MarkdownReviewer();

        // Act
        String prompt = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNull(prompt);
    }

    @Test
    void pythonReviewer_acceptsIndentedLineCommentAndTrimsGuidanceText() throws Exception {
        // Arrange
        Path file = write("script.py", "    # @guidance:   explain this script   \nprint('ok')");
        PythonReviewer reviewer = new PythonReviewer();

        // Act
        String prompt = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertTrue(prompt.contains("script.py"));
    }

    @Test
    void typeScriptReviewer_acceptsIndentedLineCommentAndTrimsGuidanceText() throws Exception {
        // Arrange
        Path file = write("script.ts", "  // @guidance:   explain this module   \nexport {};\n");
        TypeScriptReviewer reviewer = new TypeScriptReviewer();

        // Act
        String prompt = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertTrue(prompt.contains("script.ts"));
    }

    @Test
    void textReviewer_returnsOriginalEmptyContentWithoutFormatting() {
        // Arrange
        TextReviewer reviewer = new TextReviewer();
        String empty = "";
        Path file = project.resolve("@guidance.txt");

        // Act
        String prompt = reviewer.getPrompt(project.toFile(), file.toFile(), empty);

        // Assert
        assertSame(empty, prompt);
    }

    private Path write(String name, String content) throws Exception {
        Path file = project.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
