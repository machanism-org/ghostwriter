
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
 * Project-file processors and the Ghostwriter command-line workflow for
 * provider-backed guidance, prompt inclusions, and reusable acts.
 * <p>
 * {@link AbstractFileProcessor} is the traversal layer. It discovers project
 * modules, recursively lists files while honoring exclusions, matches files by
 * path, glob, or regular-expression rules, and can process modules concurrently.
 * {@link AIFileProcessor} builds the provider layer on top of it: it resolves a
 * provider or model, supplies project-relative processing metadata, substitutes
 * public configuration values, registers function tools, expands prompt
 * references, and supports file, folder, module, and pattern processing.
 * </p>
 * <h2>AI prompts and execution context</h2>
 * <p>
 * Prompt front matter may specify {@code gw.model} and {@code enabledTools}.
 * The latter accepts a scalar or YAML list. A line beginning with
 * {@link AIFileProcessor#FILE_INCLUDED_MARKER} includes UTF-8 text from an
 * {@code http://}, {@code https://}, or project-relative {@code file://}
 * reference; included text is parsed recursively. Public properties, including
 * the {@code public.} and {@code default.public.} groups, can be referenced as
 * {@code ${public.projectName}}. Interactive processing uses {@code .} to exit,
 * {@code >} to accept the current response, and {@code >>} to continue without
 * further interaction. {@link AIFileProcessor#getProcessInfo(ProjectLayout, File)}
 * exposes the processed relative path, processing mode, and operating-system
 * name to the provider.
 * </p>
 * <h2>Inline guidance</h2>
 * <p>
 * {@link GuidanceProcessor} selects a {@code Reviewer} through
 * {@link java.util.ServiceLoader} according to the file extension, extracts
 * comments marked by {@link GuidanceProcessor#GUIDANCE_TAG_NAME}, and sends the
 * resulting guidance to the provider. Reviewers preserve the marker at its
 * source location so it remains discoverable on later runs. A configured
 * default prompt can process matching files without inline guidance, and
 * {@link GuidanceProcessor#getReport()} records relative file paths and provider
 * messages.
 * </p>
 * <h2>Acts and episodes</h2>
 * <p>
 * {@link ActProcessor} loads TOML acts from built-in {@code /acts/} resources,
 * local directories, HTTP or HTTPS locations, or explicit {@code .toml} files.
 * Acts can inherit through {@code basedOn}; {@code ${super.value}} inserts an
 * inherited string or prompt value. TOML {@code default} properties provide
 * fallbacks, and {@code public.prompt} exposes the command prompt to templates.
 * A leading {@code >} expands to the ad-hoc {@code task} act. An act suffix such
 * as {@code review#1,3!} selects episodes 1 and 3 and prevents normal-order
 * continuation. {@link Episodes} executes ordered prompts sequentially, as a
 * selected subset, repeatedly, or after numeric and heading-name jumps, and
 * provides execution metadata. Episode front matter can request
 * {@code enabledTools: auto}, optionally with a selection constraint;
 * {@link EpisodeNotFoundException} reports an unknown heading-name destination.
 * </p>
 * <h2>CLI and supporting types</h2>
 * <p>
 * {@link Ghostwriter} parses command-line and properties-file settings, selects
 * guidance mode by default, or selects act mode with {@code --act}.
 * {@link GWConstants} defines shared configuration keys and formatting values,
 * while {@link ProjectContextKey} names project-layout metadata registered for
 * project-context tools.
 * </p>
 * <h2>Typical usage</h2>
 * <pre>{@code
 * AIFileProcessor processor = new AIFileProcessor(projectDir, configurator, "openai:model");
 * processor.setInstructions("Follow the project's coding standards.");
 * processor.process(projectLayout, file, "Review this file.");
 *
 * GuidanceProcessor guidance = new GuidanceProcessor(projectDir, "openai:model", configurator);
 * guidance.process(projectLayout, file, "Apply the file's inline guidance.");
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
