
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
 * Processors for applying generative-AI workflows to project files, inline
 * guidance, and reusable TOML-defined acts.
 * <p>
 * {@link org.machanism.machai.gw.processor.AIFileProcessor AIFileProcessor}
 * is the common project-aware execution base. It supplies the current file's
 * relative path, operating system, and interactive-processing mode to the
 * provider; resolves public configuration placeholders; registers function
 * tools; and can process files, folders, modules, or path-pattern matches.
 * Prompts can begin with YAML front matter: {@code gw.model} selects a provider
 * or model for that request and {@code enabledTools} selects provider tools.
 * An {@code enabledTools} value may be a scalar or YAML list.
 * </p>
 * <h2>Prompt features</h2>
 * <ul>
 * <li>Lines beginning with {@link org.machanism.machai.gw.processor.AIFileProcessor#FILE_INCLUDED_MARKER}
 * may include UTF-8 prompt content from {@code http://}, {@code https://}, or
 * project-relative {@code file://} URLs. Included content is processed
 * recursively.</li>
 * <li>Properties under the public configuration prefixes can be substituted in
 * prompts, for example {@code ${public.projectName}}.</li>
 * <li>In interactive mode, {@code .} ends processing, {@code >} accepts the
 * response and continues, and {@code >>} switches to non-interactive
 * processing.</li>
 * </ul>
 *
 * <h2>Guidance-driven processing</h2>
 * <p>
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor GuidanceProcessor}
 * scans supported source types through registered reviewers and sends inline
 * guidance marked with {@link org.machanism.machai.gw.processor.GuidanceProcessor#GUIDANCE_TAG_NAME}
 * to the provider. The marker comments remain in their source locations so
 * later runs can discover them again. A configured default prompt also permits
 * processing matching files that do not contain inline guidance, while
 * {@link org.machanism.machai.gw.processor.GuidanceProcessor#getReport()}
 * exposes file-relative results.
 * </p>
 *
 * <h2>Act and episode workflows</h2>
 * <p>
 * {@link org.machanism.machai.gw.processor.ActProcessor ActProcessor} loads
 * reusable acts from built-in {@code /acts/} resources, local locations, HTTPS
 * or HTTP locations, and explicit {@code .toml} files. Acts support inheritance
 * through {@code basedOn}; {@code ${super.value}} incorporates inherited string
 * and prompt values. TOML {@code default} properties provide fallback values,
 * while {@code public.prompt} makes the command prompt available to templates.
 * A leading {@code >} is shorthand for the ad-hoc {@code task} act.
 * </p>
 * <p>
 * An act name can select episodes using {@code #}, such as {@code review#1,3}.
 * Adding {@code !}, for example {@code review#1!}, prevents subsequent
 * normal-order execution. Episodes are ordered prompts that can be run
 * sequentially, as an explicit selected subset, repeatedly, or by a requested
 * numeric or heading-name jump; their execution metadata identifies the current
 * episode and all available episode names.
 * </p>
 * <p>
 * Episode YAML front matter may set {@code enabledTools: auto}, asking the act
 * processor to choose tools appropriate to that episode. An {@code auto}
 * mapping may also provide a selection constraint.
 * </p>
 *
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
 * @see org.machanism.machai.gw.processor.AIFileProcessor
 * @see org.machanism.machai.gw.processor.GuidanceProcessor
 * @see org.machanism.machai.gw.processor.ActProcessor
 * @see org.machanism.machai.gw.processor.Episodes
 */
package org.machanism.machai.gw.processor;
