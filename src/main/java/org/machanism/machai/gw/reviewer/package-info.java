/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 */

/**
 * Provides format-specific adapters for detecting guidance in project files
 * and preparing prompt fragments for the Ghostwriter processing pipeline.
 *
 * <p>{@link Reviewer} is the package's service-provider interface. Each
 * implementation handles a particular source format, exposes file extensions
 * that callers can use for initial dispatch, and implements
 * {@link Reviewer#perform(java.io.File, java.io.File)} to test a candidate file
 * and produce a formatted prompt fragment. Extension dispatch is only a
 * convenience: reviewers apply their own format-specific detection rules.
 * Implementations do not discover files, traverse directories, register
 * providers, or submit prompts; those responsibilities belong to the caller.
 *
 * <p>Reviewers read source text as UTF-8 and use the supplied project root to
 * provide project-relative path context where required by their prompt
 * templates. A file that has no recognized guidance normally produces
 * {@code null}; file-reading failures are reported as {@link java.io.IOException}.
 * {@link JavaReviewer} also rejects malformed UTF-8 by throwing
 * {@link IllegalArgumentException}. The exact text returned for each format is
 * defined by its localized message template in the {@code document-prompts}
 * resource bundle, so callers should treat prompt fragments as opaque.
 *
 * <p>The built-in adapters recognize the following formats and supply these
 * inputs to their prompt templates:
 * <ul>
 * <li>{@link JavaReviewer} supports {@code java} files and detects the
 *     {@code @guidance} tag in Java comments. Ordinary Java files contribute
 *     their name, project-relative path, and complete source; a file named
 *     {@code package-info.java} contributes its project-relative path.</li>
 * <li>{@link HtmlReviewer} supports {@code html}, {@code htm}, and {@code xml}
 *     files. It recognizes the tag in HTML/XML comments and supplies the file
 *     name, project-relative path, and complete source.</li>
 * <li>{@link MarkdownReviewer} supports {@code md} files. It recognizes the
 *     tag in an HTML comment and supplies the project-relative path and
 *     complete document.</li>
 * <li>{@link PythonReviewer} supports {@code py} files. It extracts non-blank
 *     guidance text following the tag in a line comment or triple-quoted
 *     string, then supplies the file name, project-relative path, and extracted
 *     text.</li>
 * <li>{@link BasicReviewer} supports VBA {@code bas}, {@code cls}, and
 *     {@code frm} files. It extracts non-blank guidance text from a VBA line
 *     comment, then supplies the file name, project-relative path, and
 *     extracted text.</li>
 * <li>{@link TypeScriptReviewer} supports {@code ts} files. It extracts
 *     non-blank guidance text from a line or block comment and supplies the
 *     file name, project-relative path, and extracted text.</li>
 * <li>{@link PumlReviewer} supports {@code puml} files. When the tag occurs in
 *     the document, it supplies the project-relative path and complete
 *     document.</li>
 * <li>{@link TextReviewer} supports {@code txt} files but processes only files
 *     named {@code @guidance.txt}. It supplies the containing directory's
 *     project-relative path and the complete text.</li>
 * </ul>
 *
 * <p>A caller selects the appropriate reviewer, passes the project root and
 * candidate file, and forwards only a non-{@code null} result to its prompt
 * consumer:
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
 * <p>Here, {@code promptPipeline} is an application-owned consumer. Additional
 * {@link Reviewer} implementations can support other formats without changing
 * this package's SPI. Such implementations should document their detection
 * rules, supported extensions, template inputs, and behavior when no guidance
 * is found.
 */
package org.machanism.machai.gw.reviewer;
