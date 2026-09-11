/*@guidance:
 * >>> file://src/guidance/package-info-code.md
 */

/**
 * Provides file-format-specific reviewers that locate {@code @guidance} instructions and convert them into
 * prompt fragments for the Ghostwriter processing pipeline.
 *
 * <p>The {@link org.machanism.machai.gw.reviewer.Reviewer Reviewer} service-provider interface defines the
 * common contract: implementations inspect a project file, return {@code null} when relevant guidance is absent,
 * or return a formatted prompt fragment when it is present. The supplied implementations support Java source,
 * HTML/XML, Markdown, PlantUML, Python, TypeScript, and dedicated {@code @guidance.txt} files. Each implementation
 * recognizes the comment convention appropriate to its format and uses the project root to include useful relative
 * path context in its result.
 *
 * <p>Clients normally select an implementation according to the file extension, then invoke
 * {@link org.machanism.machai.gw.reviewer.Reviewer#perform(java.io.File, java.io.File) perform} with the project
 * directory and candidate file. For example:
 *
 * <pre>
 * Reviewer reviewer = new JavaReviewer();
 * String prompt = reviewer.perform(projectDirectory, sourceFile);
 * if (prompt != null) {
 *     // Send the formatted guidance to the downstream pipeline.
 * }
 * </pre>
 *
 * <p>{@link org.machanism.machai.gw.reviewer.JavaReviewer JavaReviewer} handles the Java-specific distinction for
 * {@code package-info.java}; {@link org.machanism.machai.gw.reviewer.TextReviewer TextReviewer} handles the
 * filename-based text guidance convention. The remaining reviewers focus on their respective document or source
 * syntaxes while consistently producing the configured prompt format.
 */
package org.machanism.machai.gw.reviewer;
