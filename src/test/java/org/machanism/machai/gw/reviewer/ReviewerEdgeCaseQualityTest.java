package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Additional boundary tests for the reviewer implementations. */
class ReviewerEdgeCaseQualityTest {

    @TempDir
    Path tempDir;

    @Test
    void supportedExtensions_areExactAndDoNotExposeMutableSharedState() {
        // Arrange
        Reviewer[] reviewers = { new HtmlReviewer(), new JavaReviewer(), new MarkdownReviewer(), new PumlReviewer(),
                new PythonReviewer(), new TextReviewer(), new TypeScriptReviewer() };
        String[][] expected = { { "html", "htm", "xml" }, { "java" }, { "md" }, { "puml" }, { "py" },
                { "txt" }, { "ts" } };

        // Act and assert
        for (int i = 0; i < reviewers.length; i++) {
            String[] first = reviewers[i].getSupportedFileExtensions();
            first[0] = "changed";
            assertArrayEquals(expected[i], reviewers[i].getSupportedFileExtensions(),
                    "each call should return an independent extension array");
        }
    }

    @Test
    void reviewers_returnNullWhenGuidanceTagIsOnlyOrdinaryText() throws IOException {
        // Arrange
        Path project = Files.createDirectory(tempDir.resolve("project"));
        String content = "The word @guidance appears in prose, but not in a supported comment.\n";
        Path file = write(project, "document.md", content);

        // Act
        String markdown = new MarkdownReviewer().perform(project.toFile(), file.toFile());
        String html = new HtmlReviewer().perform(project.toFile(), write(project, "document.html", content).toFile());
        String puml = new PumlReviewer().perform(project.toFile(), write(project, "document.puml", "ordinary prose").toFile());

        // Assert
        assertNull(markdown);
        assertNull(html);
        assertNull(puml);
    }

    @Test
    void javaReviewer_extractPackageName_handlesValidAndDefaultPackages() {
        // Arrange
        String declaredPackage = "/* header */\npackage com.example.feature;\nclass Example {}";
        String defaultPackage = "class Example {}";

        // Act
        String actualDeclared = JavaReviewer.extractPackageName(declaredPackage);
        String actualDefault = JavaReviewer.extractPackageName(defaultPackage);

        // Assert
        assertEquals("com.example.feature", actualDeclared);
        assertEquals("<default package>", actualDefault);
    }

    @Test
    void textReviewer_perform_ignoresFilesWithSimilarButDifferentNames() throws IOException {
        // Arrange
        Path project = Files.createDirectory(tempDir.resolve("project"));
        Path file = write(project, "docs/guidance.txt", "@guidance: content");

        // Act
        String result = new TextReviewer().perform(project.toFile(), file.toFile());

        // Assert
        assertNull(result);
    }

    private Path write(Path project, String relativePath, String content) throws IOException {
        Path file = project.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }
}
