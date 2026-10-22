
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
 * Provides Ghostwriter's project traversal, prompt processing, inline-guidance,
 * and act-execution pipeline.
 *
 * <p>
 * The package is organized in layers. {@link AbstractFileProcessor} supplies the
 * filesystem layer: it obtains module information from
 * {@link org.machanism.machai.project.layout.ProjectLayout}, traverses project
 * files, applies exact-path and {@code glob:} or {@code regex:} filters, honors
 * exclusions, and can process modules with a bounded worker pool. Subclasses
 * implement the work performed for files, modules, and parent directories while
 * sharing project-relative path handling and a layered mutable configuration.
 * </p>
 *
 * <h2>Provider-backed file processing</h2>
 * <p>
 * {@link AIFileProcessor} builds on that traversal layer and delegates prompts to
 * a selected {@link org.machanism.machai.process.provider.ProcessProvider}. It
 * registers discovered and explicitly added function tools, records project
 * layout values for context tools, and supplies processing context containing the
 * processed relative path and operating-system name. It accepts YAML front matter
 * at the beginning of prompts. The {@code gw.model} property selects a provider
 * or model for that request; {@code enabledTools} accepts a scalar, YAML list, or
 * mapping; and other front-matter properties are added to the layered
 * configuration for substitution. The {@code errorHandling} property is passed to
 * the provider when present.
 * </p>
 * <p>
 * A line beginning with {@link AIFileProcessor#FILE_INCLUDED_MARKER} can include
 * UTF-8 content from an HTTP or HTTPS URL or from a project-relative
 * {@code file://} reference. Included content is parsed recursively, and public
 * configuration groups named by {@link AIFileProcessor#PUBLIC_PROP_GROUP_NAME}
 * are available to templates, including {@code ${public.projectName}}. Interactive
 * processing recognizes {@code .} to terminate, {@code >} to accept the current
 * response, and {@code >>} to continue without another input prompt.
 * </p>
 *
 * <h2>Inline guidance workflow</h2>
 * <p>
 * {@link GuidanceProcessor} specializes the provider-backed processor for source
 * files containing {@link GuidanceProcessor#GUIDANCE_TAG_NAME}. It discovers
 * {@link org.machanism.machai.gw.reviewer.Reviewer} implementations with
 * {@link java.util.ServiceLoader}, selects reviewers by normalized file
 * extension, and lets each reviewer extract guidance while preserving the marker
 * at its source location. The bundled guidance rules are sent with extracted
 * guidance. A configured default prompt can process matching files without an
 * inline marker. Results contain a project-relative file path and provider
 * message, while {@link GuidanceProcessor#getProcessedFiles()} counts attempts
 * that produced a normalized report response.
 * </p>
 *
 * <h2>Act and episode workflow</h2>
 * <p>
 * {@link ActProcessor} loads TOML act definitions from the built-in
 * {@code /acts/} classpath resources, configured local directories, HTTP or
 * HTTPS locations, or explicit TOML/content files. Acts may inherit through
 * {@code basedOn}; {@code ${super.value}} splices inherited values into child
 * properties. {@code default.*} entries provide fallbacks and
 * {@code public.prompt} exposes the command prompt to act templates. A leading
 * {@code >} expands an ad-hoc command into the {@code task} act. An act suffix,
 * such as {@code review#1,3!}, selects episodes 1 and 3 and disables continuation
 * in normal order. Act outputs and merged properties are available through
 * {@link ActProcessor#getResults()} and {@link ActProcessor#getActProperties()}.
 * Episode front matter may request {@code enabledTools: auto}, optionally with a
 * mapping that constrains provider-assisted tool selection.
 * </p>
 * <p>
 * {@link Episodes} owns the ordered prompts of an act. It supports regular
 * execution, explicitly selected 1-based episode IDs, repeat callbacks, numeric
 * jumps, and heading-name jumps. It validates selected IDs, records callback
 * results through its owning act processor, and exposes episode names and current
 * episode metadata through {@link Episodes#getActInformation(int)}. An unknown
 * heading destination raises {@link EpisodeNotFoundException}.
 * </p>
 *
 * <h2>Command-line entry point and shared types</h2>
 * <p>
 * {@link Ghostwriter} parses command-line options and configuration properties,
 * applies precedence, and selects guidance mode by default or act mode through
 * {@code --act}. It configures project, model, instruction, exclusion, thread,
 * and scan-path settings, supports multi-line console input, and translates
 * processing failures into exit codes. {@link GWConstants} centralizes the
 * configuration keys and formatting values. {@link ProjectContextKey} names the
 * operating-system, project identity, layout, source, test, documentation, and
 * module values stored for project-context tools.
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
 */
package org.machanism.machai.gw.processor;
