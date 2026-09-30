/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 */

/**
 * Provides the file-format-specific review layer for the Ghostwriter guidance
 * processing pipeline. A reviewer detects a format-specific guidance marker,
 * reads the candidate file, adds project-relative context where the prompt
 * format requires it, and returns a localized prompt fragment for downstream
 * processing.
 *
 * <p>{@link Reviewer} is the package's service-provider interface. Its
 * {@link Reviewer#perform(java.io.File, java.io.File) perform} operation receives
 * the project root and candidate file and returns {@code null} when the file
 * does not satisfy the implementation's guidance convention. Implementations
 * advertise the extensions they can inspect through
 * {@link Reviewer#getSupportedFileExtensions()}; the caller is responsible for
 * choosing a reviewer and for deciding how returned prompt fragments are
 * ordered and submitted.
 *
 * <p>The concrete reviewers combine format detection with prompt construction
 * as follows:
 * <ul>
 * <li>{@link JavaReviewer} handles Java comments and gives
 *     {@code package-info.java} package-level treatment by returning the
 *     package-info prompt without its complete source content. Other matching
 *     Java files contribute their complete UTF-8 source content.</li>
 * <li>{@link HtmlReviewer} handles HTML and XML comment blocks.</li>
 * <li>{@link MarkdownReviewer} handles guidance in Markdown HTML comments and
 *     includes the complete UTF-8 document in its prompt.</li>
 * <li>{@link PythonReviewer} handles guidance in Python line comments and
 *     triple-quoted strings, returning the extracted non-blank guidance text.</li>
 * <li>{@link TypeScriptReviewer} handles guidance in TypeScript line and block
 *     comments, returning the extracted non-blank guidance text.</li>
 * <li>{@link PumlReviewer} handles PlantUML files containing the guidance tag
 *     and includes the complete UTF-8 document in its prompt.</li>
 * <li>{@link TextReviewer} handles only files named {@code @guidance.txt} and
 *     formats their complete text with the containing directory's context.</li>
 * </ul>
 *
 * <p>All reviewers use UTF-8 input and the {@code document-prompts} resource
 * bundle to create their results. The bundle keys and argument order are
 * format-specific, so callers should treat the returned string as an opaque
 * prompt fragment rather than depending on its presentation. File-reading
 * failures are reported as {@link java.io.IOException}; a syntactically valid
 * file that has no matching guidance is represented by {@code null}.
 *
 * <p>A caller can select a reviewer by extension, invoke it with the project
 * directory and candidate file, and forward a non-{@code null} result:
 *
 * <pre>
 * Reviewer reviewer = new JavaReviewer();
 * String prompt = reviewer.perform(projectDirectory, sourceFile);
 * if (prompt != null) {
 *     promptPipeline.accept(prompt);
 * }
 * </pre>
 *
 * <p>The package does not prescribe project traversal, reviewer registration,
 * prompt ordering, or delivery. Those responsibilities remain with the
 * processor and downstream clients, which permits additional
 * {@link Reviewer} implementations to be introduced without changing the
 * package contract.
 */
package org.machanism.machai.gw.reviewer;
