/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 * - Explain the direct functional purpose, all parameters (`@param`), return values (`@return`), and exceptions thrown (`@throws`).
 * - **AI Metaprogramming Metadata:** If a class or method is annotated with `@Tool`, `@Prompt`, or `@Resource`, explicitly document its role as a "Functional AI Tool", "Prompt Template", or "Contextual Resource" respectively.
 * >>> ${guidances}/javadoc-format.md
 */

/**
 * Supplies the host-side, AI-callable tools used by the Ghostwriter runtime.
 *
 * <p>This package is the integration boundary between an AI provider and a
 * project-scoped workflow. Its {@link org.machanism.machai.process.tools.FunctionTools}
 * implementations expose annotated operations to the provider, while the
 * Ghostwriter processors perform the corresponding workflow work. File and
 * command paths are resolved against the active project directory; asynchronous
 * operations persist serialized results or logs below the runtime temporary
 * directory and return an identifier for later polling or inspection.</p>
 *
 * <h2>AI metadata</h2>
 * <p>Every method annotated with
 * {@link org.machanism.machai.process.tools.Tool @Tool} is a <em>Functional AI
 * Tool</em>. Its name, parameter descriptions, defaults, return contract, and
 * failure behavior are published as a callable operation. Methods annotated
 * with {@link org.machanism.machai.process.tools.Prompt @Prompt} are <em>Prompt
 * Templates</em>; they provide reusable instructions from the
 * {@code mcp-prompts} resource bundle. This package contains no class or method
 * annotated with {@code @Resource}, and therefore declares no <em>Contextual
 * Resource</em>. Package-level Javadoc has no callable parameters, return value,
 * or thrown exception; the {@code @param}, {@code @return}, and {@code @throws}
 * contracts for each public constructor and method are documented on the
 * declaring element.</p>
 *
 * <h2>Tool providers</h2>
 * <ul>
 * <li>{@link org.machanism.machai.gw.tools.ActFunctionTools} is the Act
 * <em>Functional AI Tool</em> provider. It loads Act definitions, performs Acts
 * synchronously or asynchronously, polls serialized results, and exposes the Act
 * <em>Prompt Template</em>.</li>
 * <li>{@link org.machanism.machai.gw.tools.GuidanceFunctionTools} discovers
 * files containing guidance tags, processes them synchronously or in the
 * background, polls processing reports, and exposes the guidance-processing
 * <em>Prompt Template</em>.</li>
 * <li>{@link org.machanism.machai.gw.tools.FileFunctionTools} lists project
 * files and folders, reads and writes bounded project files, and applies
 * validated unified or simplified patches through
 * {@link org.machanism.machai.gw.tools.PatchApplier}.</li>
 * <li>{@link org.machanism.machai.gw.tools.WebFunctionTools} retrieves HTTP(S)
 * responses or project-scoped {@code file:} content, supports CSS selection and
 * text rendering, and performs REST requests with headers and Basic
 * authentication.</li>
 * <li>{@link org.machanism.machai.gw.tools.CommandFunctionTools} executes
 * project-bounded operating-system commands, captures bounded output, and
 * provides log paging and regular-expression search. It delegates deny-list
 * checks to {@link org.machanism.machai.gw.tools.CommandSecurityChecker} and
 * records output and timing reports through
 * {@link org.machanism.machai.gw.tools.LogBuilder}.</li>
 * <li>{@link org.machanism.machai.gw.tools.ProjectContextFunctionTools} stores,
 * retrieves, pushes, and pops project-scoped workflow state. These values are
 * shared by Acts and episodes and are not operating-system environment
 * variables.</li>
 * </ul>
 *
 * <h2>Workflow control and failures</h2>
 * <p>{@link org.machanism.machai.gw.tools.ActSpecFunctionTools} is restricted
 * to Act processing and signals episode navigation or repetition through
 * {@link org.machanism.machai.gw.tools.MoveToEpisodeException} and
 * {@link org.machanism.machai.gw.tools.RepeatEpisodeException}. In contrast,
 * {@link org.machanism.machai.gw.tools.CommandSpecFunctionTools} is restricted
 * to file-processing workflows and signals task completion or application
 * termination through {@link org.machanism.machai.gw.tools.EndTaskException} and
 * {@link org.machanism.machai.gw.tools.ProcessTerminationException}.
 * {@link org.machanism.machai.gw.tools.DenyException} represents rejection by
 * command security policy rather than an ordinary command result.</p>
 *
 * <h2>Typical usage</h2>
 * <p>Register the provider applicable to the active processor, then invoke the
 * operation exposed by its {@code @Tool} metadata. A project-relative editing
 * workflow may read a source file, apply a targeted patch, and run an allowed
 * verification command:</p>
 * <pre>
 * read_file(path: "src/main/java/Example.java")
 * apply_patch_to_file(file: "src/main/java/Example.java", patch: patchText)
 * run_sys_command(command: "mvn test", dir: ".")
 * </pre>
 *
 * <p>Callers must provide values matching each operation's {@code @Param}
 * declarations and handle its documented return value and exceptions. Command
 * execution can reject unsafe input, while the control-flow exceptions above
 * intentionally request workflow transitions rather than indicating ordinary
 * tool failure. Asynchronous Act and guidance calls return a process identifier;
 * the corresponding result operation reports whether processing is complete.</p>
 *
 * @author Viktor Tovstyi
 */
package org.machanism.machai.gw.tools;
