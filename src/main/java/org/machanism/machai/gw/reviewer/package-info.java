/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 */

/**
 * Provides the file-format-specific review layer for the Ghostwriter guidance
 * processing pipeline.
 *
 * <p>A {@link Reviewer} is a format adapter: it recognizes the guidance
 * convention supported by a file type, reads the candidate file as UTF-8,
 * computes project-relative context when required by the prompt format, and
 * returns a localized prompt fragment. A file that does not contain guidance
 * for the adapter is represented by {@code null}; I/O failures are propagated
 * as {@link java.io.IOException}.
 *
 * <p>{@link Reviewer} is the package's service-provider interface. Its
 * {@link Reviewer#perform(java.io.File, java.io.File) perform} operation
 * receives the project root and candidate file. Implementations advertise the
 * extensions they can inspect through
 * {@link Reviewer#getSupportedFileExtensions()}; the caller chooses an
 * appropriate reviewer and decides how non-{@code null} prompt fragments are
 * ordered and submitted. Extension matching alone does not replace each
 * reviewer's format-specific guidance detection.
 *
 * <p>The concrete reviewers combine format detection with prompt construction
 * as follows:
 * <ul>
 * <li>{@link JavaReviewer} handles Java comments and gives
 *     {@code package-info.java} package-level treatment by returning the
 *     package-info prompt without its complete source content. Other matching
 *     Java files contribute their complete UTF-8 source content.</li>
 * <li>{@link HtmlReviewer} handles HTML and XML comment blocks and includes
 *     the complete UTF-8 source in its prompt.</li>
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
 * <p>All reviewers use the {@code document-prompts} resource bundle to create
 * localized results; the bundle keys and argument order are format-specific.
 * Callers should therefore treat each returned string as an opaque prompt
 * fragment rather than depending on its presentation. The reviewers do not
 * traverse directories, register themselves, or submit prompts.
 *
 * <p>A caller can select a reviewer by extension, invoke it with the project
 * directory and candidate file, and forward a non-{@code null} result:
 *
 * <pre>
 * File projectDirectory = new File(".");
 * File sourceFile = new File(projectDirectory, "src/main/java/Example.java");
 * Reviewer reviewer = new JavaReviewer();
 * String prompt = reviewer.perform(projectDirectory, sourceFile);
 * if (prompt != null) {
 *     promptPipeline.accept(prompt);
 * }
 * </pre>
 *
 * <p>In the example, {@code promptPipeline} represents an application-owned
 * consumer; production code can select a reviewer by checking the extension
 * returned by {@link Reviewer#getSupportedFileExtensions()} and can provide a
 * different reviewer for each supported format. Additional
 * {@link Reviewer} implementations can consequently be introduced without
 * changing this package contract.
 */
package org.machanism.machai.gw.reviewer;
