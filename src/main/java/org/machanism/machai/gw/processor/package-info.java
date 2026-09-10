
/*-
 * @guidance:
 * >>> file://src/guidance/package-info-code.md
 * - Describe all supported features based on javadoc information from:
 *     - AIFileProcessor
 *     - GuidanceProcessor
 *     - ActProcessor
 *     - Episodes
 */

/**
 * Processing infrastructure for Ghostwriter's AI-assisted project workflows.
 * <p>
 * This package supplies a common file-processing foundation and three principal
 * workflows: direct AI processing through {@link org.machanism.machai.gw.processor.AIFileProcessor},
 * inline-guidance processing through {@link org.machanism.machai.gw.processor.GuidanceProcessor},
 * and named TOML act execution through {@link org.machanism.machai.gw.processor.ActProcessor}.
 * Each workflow operates with a project layout, makes project metadata available
 * to provider tools, and can process an individual file, a directory, or files
 * selected by a path, glob, or regular-expression pattern.
 * </p>
 *
 * <h2>AI file processing</h2>
 * <p>
 * {@code AIFileProcessor} sends instructions and prompts to a configured GenAI
 * provider. Prompts may begin with YAML front matter: {@code gw.model} selects
 * a provider or model for that request, and {@code enabledTools} selects tools
 * by name (as a scalar or YAML list). Other front-matter properties remain
 * available to configuration substitution. Public configuration values whose
 * keys begin with {@code public.} or {@code default.public.} can be referenced
 * in prompt text, for example {@code ${public.projectName}}.
 * </p>
 * <p>
 * A line beginning with {@code >>>} includes UTF-8 content from an
 * {@code http://}, {@code https://}, or project-relative {@code file://}
 * reference; included content is processed recursively. Interactive processing
 * accepts {@code .} to end successfully, {@code >} to accept the current
 * response and continue, and {@code >>} to continue non-interactively.
 * Processing metadata provided to the AI includes the project-relative file
 * path, whether processing is interactive, and the operating-system name.
 * </p>
 *
 * <h2>Guidance processing</h2>
 * <p>
 * {@code GuidanceProcessor} discovers file-type reviewers through
 * {@link java.util.ServiceLoader}, extracts instructions from supported source
 * comments marked with {@code @guidance:}, and submits those instructions for
 * processing. Guidance markers are retained in their original locations so the
 * same file can be processed again. A configured default prompt also permits
 * processing matching files that have no guidance marker; results are available
 * as file/message report entries.
 * </p>
 *
 * <h2>Act processing and episodes</h2>
 * <p>
 * {@code ActProcessor} loads named {@code .toml} definitions from bundled
 * {@code /acts/} resources, a project-relative or absolute location, or an
 * HTTP(S) location. Acts can inherit another act through {@code basedOn}; the
 * {@code ${super.value}} placeholder incorporates inherited string or prompt
 * values. The {@code default} TOML section supplies fallback properties, while
 * {@code public.prompt} exposes the user-supplied prompt to templates. A command
 * starting with {@code >} is an ad-hoc {@code task}; appending {@code #} plus
 * comma-separated episode IDs selects episodes, and a trailing {@code !} stops
 * normal sequential execution afterward.
 * </p>
 * <p>
 * {@link org.machanism.machai.gw.processor.Episodes} executes ordered prompt
 * episodes in regular or selected order, supports repeat and named or numeric
 * episode moves, recognizes optional {@code # Name} headings, and publishes act
 * and current-episode metadata. An act episode can use YAML
 * {@code enabledTools: auto} to request automatic tool selection; an
 * {@code auto} mapping may provide selection constraints.
 * </p>
 *
 * <h2>Usage examples</h2>
 *
 * <pre>
 * AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "openai:model");
 * processor.setInstructions("Follow the project standards.");
 * processor.process(projectLayout, file, "Review this file.");
 *
 * GuidanceProcessor guidance = new GuidanceProcessor(projectDir, "openai:model", configurator);
 * guidance.process(projectLayout, file, "Apply inline guidance.");
 *
 * ActProcessor acts = new ActProcessor(projectDir, "openai:model", configurator);
 * acts.setAct("review#1,3! Check error handling");
 * acts.processFolder(projectLayout);
 * </pre>
 *
 * @see org.machanism.machai.gw.processor.AIFileProcessor
 * @see org.machanism.machai.gw.processor.GuidanceProcessor
 * @see org.machanism.machai.gw.processor.ActProcessor
 * @see org.machanism.machai.gw.processor.Episodes
 */
package org.machanism.machai.gw.processor;
