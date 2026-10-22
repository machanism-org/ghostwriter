package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

class HtmlReviewerTest {

    @TempDir
    Path tempDir;

    @Test
    void getSupportedFileExtensions_returnsHtmlHtmXml() {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();

        // Act
        String[] result = reviewer.getSupportedFileExtensions();

        // Assert
        assertArrayEquals(new String[] { "html", "htm", "xml" }, result);
    }

    @Test
    void getSupportedFileExtensions_returnsFreshArrayForEachCall() {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();

        // Act
        String[] first = reviewer.getSupportedFileExtensions();
        first[0] = "changed";
        String[] second = reviewer.getSupportedFileExtensions();

        // Assert
        assertEquals("html", second[0]);
        assertFalse(first == second);
    }

    @Test
    void perform_returnsNullWhenNoGuidanceCommentPresent() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        Path file = writeFile(project, "index.html", "<html></html>");

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void perform_returnsNullWhenGuidanceTagIsNotInHtmlComment() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        Path file = writeFile(project, "index.html", "<div>@guidance: ignore</div>");

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    @Test
    void perform_formatsHtmlWhenGuidanceCommentPresent() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        Path file = writeFile(project, "web/index.html",
                "<!-- @guidance: include -->\n<html><body>Hi</body></html>\n");

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Path: `web/index.html`."));
    }

    @Test
    void perform_acceptsHtmlCommentWithWhitespaceAndMultilineGuidance() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        Path file = writeFile(project, "page.htm",
                "<!--   @guidance: first line\nsecond line   -->\n<body/>\n");

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Path: `page.htm`."));
    }

    @Test
    void perform_usesFirstMatchingCommentAndDoesNotRequireGuidanceText() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        Path file = writeFile(project, "empty.xml", "<!-- @guidance: -->\n<root/>\n");

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Path: `empty.xml`."));
    }

    @Test
    void perform_preservesUtf8InputWhileDeterminingReviewability() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        String content = "<!-- @guidance: résumé -->\n<p>こんにちは</p>";
        Path file = writeFile(project, "unicode.html", content);

        // Act
        String result = reviewer.perform(project.toFile(), file.toFile());

        // Assert
        assertNotNull(result);
        assertTrue(Files.readAllLines(file, StandardCharsets.UTF_8).get(1).contains("こんにちは"));
    }

    @Test
    void perform_throwsIOExceptionWhenFileCannotBeRead() throws IOException {
        // Arrange
        HtmlReviewer reviewer = new HtmlReviewer();
        Path project = createProject();
        File missingFile = project.resolve("missing.html").toFile();

        // Act and Assert
        assertThrows(IOException.class, () -> reviewer.perform(project.toFile(), missingFile));
    }

    private Path createProject() throws IOException {
        Path project = tempDir.resolve("project");
        Files.createDirectories(project);
        return project;
    }

    private Path writeFile(Path project, String relativePath, String content) throws IOException {
        Path file = project.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
