
/*-
 * @guidance:
 * >>> ${guidances}/package-info-javadoc.md
 * - Describe all supported features based on javadoc information from:
 *     - AIFileProcessor
 *     - GuidanceProcessor
 *     - ActProcessor
 *     - Episodes
 */

/**
 * Provides the filesystem processors and command-line orchestration used by
 * Ghostwriter (GW). The package separates filesystem traversal from provider
 * invocation, then builds specialized workflows for inline guidance and TOML
 * acts.
 *
 * <h2>Processor architecture</h2>
 * <p>
 * {@link AbstractFileProcessor} is the traversal foundation. It discovers
 * project modules through {@link org.machanism.machai.project.layout.ProjectLayout},
 * recursively lists files, applies exclusion rules, supports exact paths and
 * {@code glob:} or {@code regex:} matchers, and can process modules concurrently.
 * Subclasses provide the per-file and parent-directory hooks while sharing
 * project-relative path handling, configuration, and shutdown behavior.
 * </p>
 * <p>
 * {@link AIFileProcessor} adds provider execution to that foundation. It selects
 * a provider or model, installs discovered and explicitly registered function
 * tools, stores project metadata for context tools, and supplies JSON processing
 * information containing {@code PROCESSED_FILE_REL_PATH}, {@code PROCESS_MODE},
 * and {@code OS_NAME}. It accepts YAML front matter at the start of prompts:
 * {@code gw.model} overrides the provider/model for that request, while
 * {@code enabledTools} accepts a scalar or YAML list. Other front-matter values
 * are added to the layered configuration and can participate in substitution.
 * </p>
 * <p>
 * Prompt and instruction lines beginning with
 * {@link AIFileProcessor#FILE_INCLUDED_MARKER} include UTF-8 content from an
 * HTTP or HTTPS URL or a project-relative {@code file://} reference. Included
 * content is parsed recursively. Public configuration groups
 * {@link AIFileProcessor#PUBLIC_PROP_GROUP_NAME} are available to templates,
 * including placeholders such as {@code ${public.projectName}}. Interactive
 * processing recognizes {@code .} to terminate, {@code >} to accept the current
 * response, and {@code >>} to continue without another interactive prompt.
 * </p>
 *
 * <h2>Inline guidance workflow</h2>
 * <p>
 * {@link GuidanceProcessor} specializes {@code AIFileProcessor} for source files
 * containing {@link GuidanceProcessor#GUIDANCE_TAG_NAME}. It discovers a
 * {@link org.machanism.machai.gw.reviewer.Reviewer} for each file extension via
 * {@link java.util.ServiceLoader}, delegates comment parsing to that reviewer,
 * and sends the extracted guidance together with the bundled guidance rules to
 * the provider. Reviewers preserve the guidance marker at its original source
 * location. A configured default prompt can process matching files without an
 * inline marker, and {@link GuidanceProcessor#getReport()} records relative file
 * paths and provider messages while {@link GuidanceProcessor#getProcessedFiles()}
 * counts attempted file processing.
 * </p>
 *
 * <h2>Act and episode workflow</h2>
 * <p>
 * {@link ActProcessor} loads TOML act definitions from the built-in
 * {@code /acts/} classpath resources, a local acts directory, an HTTP or HTTPS
 * location, or an explicit TOML file. Definitions can inherit through
 * {@code basedOn}; {@code ${super.value}} inserts the inherited string or prompt
 * value. {@code default.*} properties provide fallbacks, and
 * {@code public.prompt} exposes the command prompt to act templates. A leading
 * {@code >} expands an ad-hoc command into the {@code task} act. An act suffix
 * such as {@code review#1,3!} selects episodes 1 and 3 and disables continuation
 * in normal order. Act results are available through
 * {@link ActProcessor#getResults()} and merged properties through
 * {@link ActProcessor#getActProperties()}.
 * </p>
 * <p>
 * {@link Episodes} owns the ordered prompts of an act. It executes them in normal
 * order, in an explicitly selected order, or repeatedly when a callback requests
 * another iteration. It also supports numeric jumps and heading-name jumps,
 * records episode results through its owning {@code ActProcessor}, and exposes
 * episode names and current-episode metadata through
 * {@link Episodes#getActInformation(int)}. Episode front matter may request
 * {@code enabledTools: auto}; an optional mapping supplies constraints to the
 * provider-assisted tool selector. An unknown heading destination is reported by
 * {@link EpisodeNotFoundException}.
 * </p>
 *
 * <h2>Command-line entry point and shared types</h2>
 * <p>
 * {@link Ghostwriter} parses command-line options and properties-file settings,
 * resolves precedence, and selects guidance mode by default or act mode through
 * {@code --act}. It also applies project, model, instruction, exclusion, thread,
 * and scan-path settings and maps processing failures to exit codes.
 * {@link GWConstants} centralizes the corresponding configuration keys and
 * formatting values. {@link ProjectContextKey} names the operating-system,
 * project identity, layout, source, test, documentation, and module metadata
 * registered for project-context tools.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GuidanceProcessor guidance = new GuidanceProcessor(projectDir, "openai:model", configurator);
 * guidance.setInstructions("Follow the project's coding standards.");
 * guidance.scanDocuments(projectDir, "glob:**&#47;*.java");
 *
 * ActProcessor acts = new ActProcessor(projectDir, "openai:model", configurator);
 * acts.setAct("review#1,3! Check correctness and error handling");
 * acts.process(projectLayout);
 * }</pre>
 *
 * @see AbstractFileProcessor
 * @see AIFileProcessor
 * @see GuidanceProcessor
 * @see ActProcessor
 * @see Episodes
 * @see Ghostwriter
 */
package org.machanism.machai.gw.processor;
