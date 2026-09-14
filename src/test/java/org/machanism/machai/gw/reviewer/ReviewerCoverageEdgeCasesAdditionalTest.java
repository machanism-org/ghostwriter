package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewerCoverageEdgeCasesAdditionalTest {

	@TempDir
	Path tempDir;

	/**
	 * Verifies that Python guidance embedded in a triple-quoted string is reviewed without error.
	 */
	@Test
	void pythonReviewer_perform_returnsResultForTripleQuotedGuidance() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("sample.py");
		String content = "\"\"\" @guidance: doc \"\"\"\nprint('ok')\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		org.junit.jupiter.api.Assertions.assertNotNull(result);
	}

	@Test
	void pythonReviewer_perform_returnsNullWhenGuidanceTextResolvesToNull() throws IOException {
		// Arrange
		PythonReviewer reviewer = new PythonReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("empty.py");
		Files.write(file, "x='@guidance'\n".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void typeScriptReviewer_perform_returnsResultForBlockGuidanceComment() throws IOException {
		// Arrange
		TypeScriptReviewer reviewer = new TypeScriptReviewer();
		Path project = tempDir.resolve("project");
		Files.createDirectories(project);
		Path file = project.resolve("app.ts");
		String content = "/* @guidance: keep */\nexport const app = 1;\n";
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(project.toFile(), file.toFile());

		// Assert
		org.junit.jupiter.api.Assertions.assertNotNull(result);
		org.junit.jupiter.api.Assertions.assertTrue(result.contains("Typescript"));
	}

	@Test
	void javaReviewer_extractPackageName_returnsDefaultPackageForInvalidDeclaration() {
		// Arrange
		String content = "package 123.invalid;\nclass Sample {}";

		// Act
		String result = JavaReviewer.extractPackageName(content);

		// Assert
		assertEquals("<default package>", result);
	}

	@Test
	void reviewerExtensions_areFormatSpecificAndStable() {
		// Arrange
		Reviewer[] reviewers = { new HtmlReviewer(), new JavaReviewer(), new MarkdownReviewer(), new PumlReviewer(),
				new PythonReviewer(), new TextReviewer(), new TypeScriptReviewer() };

		// Act
		String[][] extensions = new String[reviewers.length][];
		for (int i = 0; i < reviewers.length; i++) {
			extensions[i] = reviewers[i].getSupportedFileExtensions();
		}

		// Assert
		assertEquals("html", extensions[0][0]);
		assertEquals("java", extensions[1][0]);
		assertEquals("md", extensions[2][0]);
		assertEquals("puml", extensions[3][0]);
		assertEquals("py", extensions[4][0]);
		assertEquals("txt", extensions[5][0]);
		assertEquals("ts", extensions[6][0]);
		assertEquals(3, extensions[0].length);
	}

	@Test
	void htmlReviewer_perform_returnsNullWithoutGuidanceComment() throws IOException {
		// Arrange
		HtmlReviewer reviewer = new HtmlReviewer();
		Path file = tempDir.resolve("plain.html");
		Files.write(file, "<!-- ordinary comment -->".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(tempDir.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void markdownReviewer_perform_returnsNullWithoutGuidanceComment() throws IOException {
		// Arrange
		MarkdownReviewer reviewer = new MarkdownReviewer();
		Path file = tempDir.resolve("plain.md");
		Files.write(file, "# heading".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(tempDir.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void pumlReviewer_perform_returnsNullWithoutGuidanceTag() throws IOException {
		// Arrange
		PumlReviewer reviewer = new PumlReviewer();
		Path file = tempDir.resolve("plain.puml");
		Files.write(file, "@startuml\nAlice -> Bob\n@enduml".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(tempDir.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}

	@Test
	void textReviewer_perform_returnsNullForNonGuidanceFile() throws IOException {
		// Arrange
		TextReviewer reviewer = new TextReviewer();
		Path file = tempDir.resolve("notes.txt");
		Files.write(file, "notes".getBytes(StandardCharsets.UTF_8));

		// Act
		String result = reviewer.perform(tempDir.toFile(), file.toFile());

		// Assert
		assertNull(result);
	}
}
