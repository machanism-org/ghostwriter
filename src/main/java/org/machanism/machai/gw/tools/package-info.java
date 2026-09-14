/*@guidance:
 * >>> ${guidances}/package-info-javadoc.md
 * - Explain the direct functional purpose, all parameters (`@param`), return values (`@return`), and exceptions thrown (`@throws`).
 * - **AI Metaprogramming Metadata:** If a class or method is annotated with `@Tool`, `@Prompt`, or `@Resource`, explicitly document its role as a "Functional AI Tool", "Prompt Template", or "Contextual Resource" respectively.
 * >>> ${guidances}/javadoc-format.md
 */

/**
 * Supplies the host-side, AI-callable tools used by the Ghostwriter runtime.
 *
 * <p>The package is the integration boundary between an AI provider and a
 * project-scoped workflow. Implementations of
 * {@link org.machanism.machai.ai.tools.FunctionTools} expose annotated methods
 * to the provider, while the processors in
 * {@link org.machanism.machai.gw.processor.ActProcessor} and
 * {@link org.machanism.machai.gw.processor.AIFileProcessor} perform the
 * corresponding workflow work. File and command paths are resolved against the
 * active project directory; asynchronous operations persist results under the
 * runtime temporary directory and return an identifier for later polling.</p>
 *
 * <h2>AI metadata and tool providers</h2>
 * <p>Methods annotated with
 * {@link org.machanism.machai.ai.tools.Tool @Tool} are <em>Functional AI
 * Tools</em>. Their names, parameter descriptions, default values, return
 * contracts, and failures are published as callable operations. Methods
 * annotated with {@link org.machanism.machai.ai.tools.Prompt @Prompt} are
 * <em>Prompt Templates</em>; they supply reusable instructions from the
 * {@code mcp-prompts} resource bundle. This package currently declares no
 * methods annotated with {@code @Resource}, so it provides no <em>Contextual
 * Resources</em>. Package-level Javadoc has no callable parameters, return
 * value, or thrown exception; those contracts are documented on each public
 * constructor and method.</p>
 *
 * <h2>Functional areas</h2>
 * <ul>
 * <li>{@link org.machanism.machai.gw.tools.ActFunctionTools} is the Act
 * <em>Functional AI Tool</em> provider. It loads Act definitions, starts
 * synchronous or asynchronous Act execution, polls serialized results, and
 * exposes the Act <em>Prompt Template</em>.</li>
 * <li>{@link org.machanism.machai.gw.tools.GuidanceFunctionTools} discovers
 * files containing guidance tags, processes them synchronously or in the
 * background, polls processing reports, and exposes the guidance-processing
 * <em>Prompt Template</em>.</li>
 * <li>{@link org.machanism.machai.gw.tools.FileFunctionTools} lists files and
 * folders, reads and writes project files, and applies validated unified or
 * simplified patches through {@link org.machanism.machai.gw.tools.PatchApplier}.
 * {@link org.machanism.machai.gw.tools.WebFunctionTools} retrieves HTTP(S)
 * responses or project-scoped {@code file:} content, supports CSS selection and
 * text rendering, and performs REST requests with headers and Basic
 * authentication.</li>
 * <li>{@link org.machanism.machai.gw.tools.CommandFunctionTools} executes
 * project-bounded operating-system commands, captures bounded output, and
 * provides log paging and regular-expression search. It delegates policy
 * checks to {@link org.machanism.machai.gw.tools.CommandSecurityChecker},
 * whose deny-list violations produce
 * {@link org.machanism.machai.gw.tools.DenyException}. Command output and
 * timing reports are maintained by {@link org.machanism.machai.gw.tools.LogBuilder}.</li>
 * <li>{@link org.machanism.machai.gw.tools.ProjectContextFunctionTools} stores,
 * retrieves, pushes, and pops project-scoped workflow state. Values are shared
 * by Acts and episodes but are not operating-system environment variables.</li>
 * <li>{@link org.machanism.machai.gw.tools.ActSpecFunctionTools} is restricted
 * to Act processing and signals episode navigation or repetition through
 * {@link org.machanism.machai.gw.tools.MoveToEpisodeException} and
 * {@link org.machanism.machai.gw.tools.RepeatEpisodeException}. 
 * {@link org.machanism.machai.gw.tools.CommandSpecFunctionTools} is restricted
 * to file-processing workflows and signals task completion or application
 * termination through {@link org.machanism.machai.gw.tools.EndTaskException}
 * and {@link org.machanism.machai.gw.tools.ProcessTerminationException}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Register the provider applicable to the active processor, then invoke the
 * operation exposed by its {@code @Tool} metadata. A project-relative editing
 * workflow may read a source file, apply a targeted patch, and run an allowed
 * verification command:</p>
 * <pre>
 * read-file(file-path: "src/main/java/Example.java")
 * apply-patch-to-file(file: "src/main/java/Example.java", patch: patchText)
 * run-sys-command(command: "mvn test", dir: ".")
 * </pre>
 *
 * <p>Callers must provide values matching each operation's {@code @Param}
 * declarations and handle its documented return value and exceptions. In
 * particular, command execution can reject unsafe input, while control-flow
 * exceptions intentionally request workflow transitions rather than indicating
 * ordinary tool failure. Asynchronous Act and guidance calls return a process
 * identifier; the corresponding result operation reports whether processing is
 * complete.</p>
 *
 * @author Viktor Tovstyi
 */
package org.machanism.machai.gw.tools;
