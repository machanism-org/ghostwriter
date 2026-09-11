/*@guidance:
 * >>> file://src/guidance/package-info-javadoc.md
 * - Explain the direct functional purpose, all parameters (`@param`), return values (`@return`), and exceptions thrown (`@throws`).
 * - **AI Metaprogramming Metadata:** If a class or method is annotated with `@Tool`, `@Prompt`, or `@Resource`, explicitly document its role as a "Functional AI Tool", "Prompt Template", or "Contextual Resource" respectively.
 * >>> file://src/guidance/javadoc-format.md
 */

/**
 * Functional AI tools and supporting infrastructure used by the Ghostwriter
 * runtime to let AI-driven Acts and processors interact with the host project
 * in a controlled manner.
 *
 * <p>The package is organized around implementations of
 * {@link org.machanism.machai.ai.tools.FunctionTools}. Their methods annotated
 * with {@link org.machanism.machai.ai.tools.Tool @Tool} are Functional AI Tools:
 * the runtime exposes their declared names, parameter metadata, results, and
 * failures to a model. Methods annotated with {@link
 * org.machanism.machai.ai.tools.Prompt @Prompt} are Prompt Templates that
 * provide reusable instructions for Act and guidance-tag workflows. The package
 * currently does not define methods annotated as Contextual Resources
 * ({@code @Resource}).</p>
 *
 * <h2>Tool groups and relationships</h2>
 * <ul>
 * <li>{@link org.machanism.machai.gw.tools.ActFunctionTools} loads and executes
 * Acts through {@link org.machanism.machai.gw.processor.ActProcessor}, including
 * asynchronous result polling.</li>
 * <li>{@link org.machanism.machai.gw.tools.GuidanceFunctionTools} discovers and
 * processes embedded guidance tags through
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor}.</li>
 * <li>{@link org.machanism.machai.gw.tools.FileFunctionTools},
 * {@link org.machanism.machai.gw.tools.CommandFunctionTools}, and
 * {@link org.machanism.machai.gw.tools.WebFunctionTools} provide project file,
 * command, and HTTP operations. File tools resolve paths beneath the project
 * root; command tools resolve a project-relative working directory, apply
 * operating-system shell handling, capture output, and use
 * {@link org.machanism.machai.gw.tools.CommandSecurityChecker} to apply
 * operating-system-specific deny-list rules. Web tools retrieve HTTP(S) content
 * or project-scoped {@code file:} URLs and support CSS selection, text rendering,
 * headers, and Basic authentication.</li>
 * <li>{@link org.machanism.machai.gw.tools.ProjectContextFunctionTools} stores
 * project-scoped workflow state shared by Acts and episodes.</li>
 * <li>{@link org.machanism.machai.gw.tools.ActSpecFunctionTools} is restricted
 * to Act processing, while {@link
 * org.machanism.machai.gw.tools.CommandSpecFunctionTools} is restricted to
 * file-processing workflows. They provide processor-specific navigation and
 * completion controls. Their exceptions,
 * including {@link org.machanism.machai.gw.tools.EndTaskException},
 * {@link org.machanism.machai.gw.tools.MoveToEpisodeException},
 * {@link org.machanism.machai.gw.tools.RepeatEpisodeException}, and
 * {@link org.machanism.machai.gw.tools.ProcessTerminationException}, intentionally
 * communicate workflow transitions to the host rather than ordinary failures.</li>
 * <li>{@link org.machanism.machai.gw.tools.LogBuilder} captures bounded command
 * output and persists full command logs for later paging and regular-expression
 * searches, while {@link org.machanism.machai.gw.tools.PatchApplier} applies
 * validated unified or simplified text patches for the file tool.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>The runtime registers the applicable tool providers for a processor and
 * invokes tools from their metadata. For example, a model can use a Functional
 * AI Tool to read a project-relative file, edit it, and run an allowed command:
 * </p>
 * <pre>
 * read-file(file-path: "src/main/java/Example.java")
 * write-file(file-path: "src/main/java/Example.java", text: updatedSource)
 * run-sys-command(command: "mvn test", dir: ".")
 * </pre>
 *
 * <p>Callers must supply values matching each tool's declared parameters and
 * handle its documented result and exception contract. In particular, command
 * execution can reject unsafe commands with {@link
 * org.machanism.machai.gw.tools.DenyException}, and asynchronous Act or guidance
 * processing returns an identifier that is later used to retrieve the result.
 * Project context values are scoped by project directory and can be shared by
 * subsequent Acts and episodes.</p>
 *
 * @author Viktor Tovstyi
 */
package org.machanism.machai.gw.tools;
