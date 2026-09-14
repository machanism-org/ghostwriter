/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 */

/**
 * Provides the file-format-specific review layer for the Ghostwriter guidance
 * processing pipeline. A reviewer identifies guidance in a supported project
 * file, gathers the context required by the downstream prompt, and returns a
 * localized prompt fragment for further processing.
 *
 * <p>{@link Reviewer} is the package's service-provider interface. Its
 * {@link Reviewer#perform(java.io.File, java.io.File) perform} operation receives
 * the project root and candidate file, computes project-relative context, and
 * returns {@code null} when the file does not satisfy the implementation's
 * guidance convention. Implementations also advertise the extensions they can
 * inspect through {@link Reviewer#getSupportedFileExtensions()}.
 *
 * <p>The concrete reviewers separate format detection from prompt construction:
 * <ul>
 * <li>{@link JavaReviewer} handles Java comments and gives
 *     {@code package-info.java} package-level treatment by omitting its complete
 *     source content from the generated prompt.</li>
 * <li>{@link HtmlReviewer} handles HTML and XML comment blocks.</li>
 * <li>{@link MarkdownReviewer} handles guidance in Markdown HTML comments.</li>
 * <li>{@link PythonReviewer} handles Python line comments and triple-quoted
 *     guidance strings.</li>
 * <li>{@link TypeScriptReviewer} handles TypeScript line and block comments.</li>
 * <li>{@link PumlReviewer} handles PlantUML files containing the guidance tag.</li>
 * <li>{@link TextReviewer} handles only files named {@code @guidance.txt} and
 *     formats their complete text with parent-directory context.</li>
 * </ul>
 *
 * <p>Each source-oriented reviewer reads UTF-8 content, detects the format's
 * supported comment or text convention, and uses the {@code document-prompts}
 * resource bundle to create its result. The bundle keys are format-specific;
 * therefore callers should treat the returned string as an opaque prompt
 * fragment rather than depending on its presentation.
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
 * <p>Implementations throw {@link java.io.IOException} when file access fails.
 * The package does not prescribe how prompts are ultimately submitted, allowing
 * the processor and downstream clients to control traversal, ordering, and
 * delivery.
 */
package org.machanism.machai.gw.reviewer;
