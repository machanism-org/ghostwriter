/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 */

/**
 * Provides format-specific adapters for the Ghostwriter guidance-processing
 * pipeline.
 *
 * <p>The central abstraction is {@link Reviewer}. A reviewer inspects one
 * candidate file, recognizes the guidance syntax supported by its format, and
 * creates a localized prompt fragment for downstream processing. Implementations
 * read source files as UTF-8 and use the project root supplied to
 * {@link Reviewer#perform(java.io.File, java.io.File) perform} to calculate
 * project-relative context where their prompt template requires it. A file
 * that has no guidance recognized by the implementation produces {@code null};
 * read failures are reported as {@link java.io.IOException}.
 *
 * <p>{@link Reviewer#getSupportedFileExtensions()} exposes the extensions a
 * caller may use for initial dispatch, without a leading period. Extension
 * dispatch is only an optimization: each reviewer still performs its own
 * format-specific guidance detection. Reviewers do not discover files,
 * register themselves, traverse directories, or submit prompts, so those
 * responsibilities remain with the calling pipeline.
 *
 * <p>The built-in adapters and their prompt inputs are:
 * <ul>
 * <li>{@link JavaReviewer}: recognizes guidance in Java line or block comments.
 *     For {@code package-info.java} it emits package-level path context rather
 *     than the complete source; other matching Java files contribute their
 *     complete UTF-8 source.</li>
 * <li>{@link HtmlReviewer}: recognizes guidance in HTML/XML comment blocks and
 *     contributes the complete UTF-8 source.</li>
 * <li>{@link MarkdownReviewer}: recognizes guidance in Markdown HTML comments
 *     and contributes the complete UTF-8 document.</li>
 * <li>{@link PythonReviewer}: recognizes guidance after a Python line-comment
 * marker or inside a triple-quoted string and returns the extracted,
 * non-blank guidance text.</li>
 * <li>{@link TypeScriptReviewer}: recognizes guidance in TypeScript line or
 *     block comments and returns the extracted, non-blank guidance text.</li>
 * <li>{@link PumlReviewer}: recognizes a PlantUML document containing the
 *     guidance tag and contributes the complete UTF-8 document.</li>
 * <li>{@link TextReviewer}: handles only files named {@code @guidance.txt} and
 *     formats their complete text with the containing directory's context.</li>
 * </ul>
 *
 * <p>All built-in reviewers obtain localized templates from the
 * {@code document-prompts} resource bundle. Template keys and argument order
 * are format-specific, so callers should treat returned strings as opaque
 * prompt fragments. A typical caller selects an adapter, invokes it, and
 * forwards only a non-{@code null} result:
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
 * {@link Reviewer} implementations can be added for other formats without
 * changing this package contract, provided they document their detection
 * rules, supported extensions, prompt-template arguments, and null-result
 * behavior.
 */
package org.machanism.machai.gw.reviewer;
