<!-- @guidance: 
Generate or update the content as follows.  
If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
Ghostwriter is an **AI-powered agent** designed to work with **all types of project files**, including source code, documentation, project website content, and any other relevant files.  
**When creating content, always keep in mind that the application works with all file types present in the project.**
# Page Structure
1. **Header**
   - **Project Title:** Extract automatically from `pom.xml`.
   - **Maven Central Badge:**  
     Use the following Markdown, replacing `[groupId]` and `[artifactId]` with values from `pom.xml`:  
     `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
   - [![Download](https://img.shields.io/sourceforge/dt/machanism/machai%2Fghostwriter%2Freleases)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/releases/)
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/ghostwriter/refs/heads/main/bindex.json)
2. **Introduction**
   - Provide a comprehensive description of the project's purpose and main benefits.
   - Reference [Guidance-Driven Processing (GDP)](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
   - Reference [Act-Driven Workflows (ADW)](https://www.machanism.org/act/index.html) as the conceptual foundation for Machai Ghostwriter.
3. **Overview**
   - Clearly explain the core functionality and value proposition of the project.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
4. **Machai Ghostwriter vs. Other Tools.** 
   - Identify the AI code assistant tool most similar to Machai Ghostwriter and explain why, focusing on project-wide automation, CI/CD integration, and extensibility.
   - List key similarities and key differences between Machai Ghostwriter and the closest tool.
   - Briefly compare Machai Ghostwriter to other popular tools (e.g., Tabnine, GitHub Copilot, Claude Code, Cursor) in terms of project-wide automation, guidance, and documentation features.
   - Summarize the comparison in a Markdown table showing which tools support project-wide automation, custom guidance, CI/CD integration, and documentation generation.
   - Conclude with a short statement on what makes Machai Ghostwriter unique.
Let me know if you want it even shorter or tailored for a specific toolset!
5. **Key Features**
   - Present a concise, bulleted list of the primary capabilities and features.
6. **Getting Started**
   - **Prerequisites:** List all required software, services, and environment settings.
7. **Machai Ghostwriter CLI Pack**  
     Add a download link for the Ghostwriter CLI delivery pack:  
     [![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
     [Bindex Core](https://machai.machanism.org/bindex-core/index.html)
   - **Basic Usage:** Provide an example command to run the application.
   - **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
   - **Java Version:** State the required Java version as defined in `pom.xml`, and clarify any additional functional requirements.
8. **Configuration**
   - **Command-Line Options:** Analyze `/java/org/machanism/machai/gw/processor/Ghostwriter.java` to extract and describe all available command-line options.
   - **Options Table:** Present a table listing each option, its description, and default value.
   - **Example:** Provide a command-line example showing how to configure and run the application with custom parameters. Include information from the `Ghostwriter.help()` method.
9. **Resources**
   - List relevant links, including the official platform, GitHub repository, and Maven Central page.
   - [Ghostwriter MCP Server](gw-mcp-server/)
# General Instructions
- Ensure clarity, completeness, and accuracy in each section.
- Use information from project files and source code as specified.
- Structure the documentation for easy navigation and practical use.
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter) [![Download](https://img.shields.io/sourceforge/dt/machanism/machai%2Fghostwriter%2Freleases)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/releases/) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/ghostwriter/refs/heads/main/bindex.json)

## Introduction

Machai Ghostwriter is an AI-powered agent for maintaining an entire project rather than only the file open in an editor. It can work with source code, tests, documentation, website content, configuration, diagrams, and other relevant project files. Guidance is kept beside the content it governs, making requested changes explicit, reviewable, and repeatable; reusable workflows extend this approach to coordinated repository tasks.

Its conceptual foundations are [Guidance-Driven Processing (GDP)](https://www.machanism.org/guided-file-processing/index.html), which places durable instructions in project artifacts, and [Act-Driven Workflows (ADW)](https://www.machanism.org/act/index.html), which models reusable, multi-step AI workflows. Together, they make it practical to automate documentation maintenance, structured project changes, and CI/CD-friendly processing while retaining project-specific intent.

## Overview

Ghostwriter resolves runtime settings and scans a selected project scope. In the default Guidance mode, format-aware reviewers discover embedded `@guidance` directives and process the files that contain them. In Act mode, it executes a selected prompt workflow against project files or directories, sharing project context across its steps without requiring embedded guidance. Configured AI providers can use registered tools for project-file operations, security-checked commands, web and REST access, and workflow control. The agent is intended to work with all types of project files; automatic guidance discovery depends on an available reviewer for the file format, while Act workflows and file tools can address other relevant artifacts.

The architecture separates command-line startup and configuration from scanning, AI orchestration, guidance handling, workflow execution, and format-specific review. These services are supported by focused tool adapters and provider management, so local project operations and remote AI services can evolve independently. Users invoke the command-line interface; it selects the processing mode and coordinates these services against project content and optional remote resources.

### Project Structure Overview

The project is organized as a command-line application with a shared processing core. Startup resolves configuration and selects either guidance processing or Act execution. Both modes reuse project and module traversal, filtering, prompt composition, and provider dispatch. Guidance processing delegates instruction extraction to format-aware reviewers; Act processing loads reusable definitions, merges inherited settings, and coordinates ordered episodes. Provider management and tool registration connect the processing core to the configured model and to controlled capabilities for files, commands, web resources, Acts, guidance, and shared context. The project layout library supplies module discovery and project-relative paths. External project content, remote resources, and the GenAI provider remain outside the application boundary, allowing the same processing flow to be used locally or in automation.

![Ghostwriter component diagram](./images/c4-diagram.png)

The related processing diagrams show the same architecture from complementary perspectives. The guidance flow scans eligible content, extracts an embedded directive with the appropriate reviewer, sends the composed request to the provider, and records a guidance report; files without applicable instructions are skipped unless a default prompt is configured. The Act flow parses and configures a workflow, selects its episodes, traverses project modules and files, and records results while allowing an episode to repeat, move, or stop processing. Together, these views clarify that guidance processing and Act processing are separate entry paths that share traversal, provider, tool, and project-context services.

See the [guidance processing flow](./images/guidance-tag-processing.png), [guidance overview](./images/guidance-tag-processing-overview.png), [Act episode flow](./images/episodes-act-processing.png), and [module and file traversal flow](./images/module-file-act-processing.png) for the detailed diagrams.

### Machai Ghostwriter vs. Other Tools

The closest widely known tool to Machai Ghostwriter is **Claude Code**. Both can work across a project's files, use tools, and assist with multi-file changes rather than only inline code completion. Claude Code is the nearest comparison because it combines an agentic CLI with headless automation, [GitHub Actions integration](https://code.claude.com/docs/en/github-actions), persistent project instructions, and extensibility through skills, hooks, and MCP tools. Ghostwriter emphasizes repository-embedded guidance, explicit scan scopes, reusable Acts, and Java CLI deployment in build pipelines.

#### Key similarities

- Both support multi-file, repository-level work.
- Both go beyond autocomplete and can perform task-oriented engineering workflows.
- Both can be used from command-line-oriented environments.
- Both support automated pipeline execution as well as local command-line use.
- Both offer reusable instructions and extensible, tool-assisted LLM execution.

#### Key differences

- **Guidance placement:** Ghostwriter discovers maintenance instructions embedded beside the governed content through `@guidance` comments. Claude Code uses persistent project instruction files, skills, and task prompts rather than Ghostwriter's per-file tag discovery model.
- **Batch selection:** Ghostwriter explicitly traverses paths, glob patterns, regex targets, and project modules. Claude Code is task-oriented and can also be scripted or scheduled, but does not use this guidance-scanning lifecycle.
- **Artifact maintenance:** Ghostwriter treats documentation, project site content, diagrams, configuration, and source code as first-class maintenance targets. Claude Code can also edit documentation and non-code files when instructed.
- **Workflow extensibility:** Ghostwriter uses inheritable Act definitions with selected, ordered episodes and registered Java tool adapters; Claude Code provides skills, hooks, plugins, and MCP integrations.
- **Deployment:** Ghostwriter is a Maven-built Java CLI that can be invoked by a CI job; Claude Code has dedicated GitHub Actions integration in addition to command-line automation. Ghostwriter's Maven build is not itself a dedicated Maven processing plugin.

#### Brief comparison with popular tools

Tabnine emphasizes IDE assistance and organization-aware context; its repository automation capabilities depend on the product and deployment. GitHub Copilot extends completions and chat with repository agents, custom instructions, and [GitHub automation workflows](https://docs.github.com/en/copilot/concepts/agents/coding-agent/about-coding-agent). Cursor combines an AI-native IDE with multi-file agents, rules, and a [headless CLI for scripts and CI](https://cursor.com/docs/cli/headless). Claude Code provides the closest command-line, repository-aware agent workflow. All of these tools can assist with documentation, but Ghostwriter makes embedded per-file guidance and repeatable artifact maintenance central to its processing model.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| Machai Ghostwriter | Yes, scoped scans and Act workflows | Yes, embedded `@guidance` and Acts | Yes, invoke the Java CLI from pipeline jobs | Yes, first-class artifact maintenance |
| Claude Code | Yes, repository agent and headless workflows | Yes, persistent project instructions and skills | Yes, GitHub Actions and headless CLI | Yes, task- or workflow-driven |
| GitHub Copilot | Yes, repository agents and automations | Yes, repository instructions and custom agents | Yes, GitHub Actions/agent workflows; feature-dependent | Yes, chat and agent tasks |
| Cursor | Yes, multi-file agents and headless scripts | Yes, project rules and skills | Yes, headless CLI; cloud features vary | Yes, interactive or scripted tasks |
| Tabnine | Product-dependent; not assumed to provide a comparable batch scanner | Yes, contextual and organization-specific guidance, depending on edition | Deployment-dependent; no comparable batch CLI asserted here | Yes, assistant-driven explanations and documentation; edition-dependent |

The table compares capabilities, not equivalent implementations or guarantees. Availability depends on tool version, plan, enabled agent features, permissions, and deployment. Documentation output and automated edits still require human review and validation.

Machai Ghostwriter is unique because it combines repository-embedded guidance, broad project-file support, reusable act workflows, and CI/CD-friendly batch execution into a governed automation model for keeping code, documentation, diagrams, and configuration aligned.

## Key Features

- Scans directories, glob patterns, and regex-based targets.
- Detects embedded `@guidance` directives in repository files.
- Processes source code, documentation, project site content, configuration, diagrams, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Supports additional system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Provides Act mode for reusable and ad-hoc prompt workflows.
- Supports configurable concurrency.
- Can log LLM request inputs for diagnostics and auditing.
- Records usage statistics at the end of processing.
- Fits both local development and CI/CD automation scenarios.

## Getting Started

### Prerequisites

- **Java runtime compatible with the delivery pack and selected provider libraries.** The Maven build sets `maven.compiler.release` to **8**, targeting Java 8 bytecode for Ghostwriter itself; bundled dependencies or provider plugins may require a newer runtime.
- A configured, supported Machai GenAI provider and model, including any required credentials, endpoint, and network access.
- Read/write access to the target project; use network access when the provider, remote Acts, or referenced HTTP(S) resources need it.
- **Maven 3.x and a compatible JDK** to build from source, plus access to the project's dependency repositories. Building the delivery pack additionally requires the `pack` profile and the `MACHANISM_PACK_DIR` environment variable.
- Configure provider credentials without committing secrets: the supplied pack documents `GENAI_USERNAME`/`GENAI_PASSWORD` for CodeMie, or `OPENAI_API_KEY` and optional `OPENAI_BASE_URL` for an OpenAI-compatible service. Requirements vary with the selected provider.
- Any tools requested by a workflow (such as Git, Maven, or test runners) must be installed and accessible to the process, and command execution must satisfy the configured security rules. Use version control to review and revert generated changes.

## Machai Ghostwriter CLI Pack

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

Download and unpack the delivery pack to run the CLI; retain its bundled libraries and plugins alongside the executable JAR. Configure the supplied properties and provider credentials before processing. See [Bindex Core](https://machai.machanism.org/bindex-core/index.html) for the related indexing component.

To build from source, use `mvn clean install`. To also assemble the executable CLI and delivery pack on Windows, set the pack output directory and enable the packaging profile:

```bat
set "MACHANISM_PACK_DIR=delivery"
mvn -Ppack clean install
```

The packaging profile creates the executable JAR in the build output and the delivery archive under the configured pack output directory. The ordinary library JAR is not a substitute for the complete CLI pack.

### Basic Usage

Run the executable JAR with a path or pattern to scan:

```bash
java -jar gw.jar "glob:**/*.md"
```

The positional `<path>` can be a relative path inside the project, a directory name, a `glob:` pattern such as `glob:**/*.java`, or a `regex:` pattern such as `regex:^.*/[^/]+\.java$`. Multiple positional targets are accepted and scanned in order. Absolute paths must remain inside the configured project directory. If no positional path is provided, Ghostwriter uses `gw.path` or `.`. Path matching uses the operating system's Java path matcher, so regular expressions involving separators may need adjustment on Windows.

### Typical Workflow

1. Unpack the CLI delivery artifacts and configure the AI provider, model, credentials, and project directory.
2. Add precise `@guidance` directives to the files that should govern their own processing, or create/select an Act workflow.
3. Start with a narrow file path or pattern and supply exclusions where needed.
4. Run the CLI, review the resulting changes and command logs, and refine the guidance or Act.
5. Reuse the same configuration and scoped command in CI/CD after validating it locally. Supply explicit instruction/Act values, choose non-interactive workflows, provision credentials through the CI secret store, and validate the resulting diff and tests before publishing.

### Java Version and Functional Requirements

The project declares Java **8** through `maven.compiler.release`. This is Ghostwriter's compilation target, not a guarantee that every bundled dependency or provider plugin runs on Java 8; use a runtime compatible with the complete delivery pack. Building requires a JDK supported by the selected Maven plugins. Functional processing also requires an available GenAI provider/model configuration; project writes require suitable file permissions, and remote providers or resources require the relevant credentials and network connectivity.

## Configuration

Explicit command-line values take precedence over the corresponding startup configuration values. Run `java -jar gw.jar --help` to print the CLI syntax, descriptions, and path examples. The current source implements this in `printHelp(...)` rather than a method named `help()`. Its usage header is `java -jar gw.jar <path> [options]`; positional targets may be relative paths, directories, `glob:` patterns, or `regex:` patterns. Without targets, `gw.path` is used, falling back to `.`. An Act or prompt can also supply its own model and processing settings.

### Command-Line Options

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Print the usage header, positional path rules, all options, and examples, then exit without processing. | Not enabled |
| `-d <dir>`, `--projectDir <dir>` | Set the project root used for scanning and processing. | `project.dir`; otherwise the current working directory (`user.dir`) |
| `-c <file>`, `--config <file>` | Select a configuration properties file. Relative paths use the initial project directory, before `--projectDir` is applied. | `gw.properties`; `-Dgw.config=...` can select another file when `--config` is absent |
| `-t <n>`, `--threads <n>` | Set module-processing concurrency; must be a positive integer. Higher values increase resource and provider use. | `gw.threads`; otherwise serial module processing (internal thread field starts at `0`) |
| `-m <provider:model>`, `--model <provider:model>` | Select the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model`, otherwise unset |
| `-i [text]`, `--instructions [text]` | Set system instructions. When used without text, prompt for the instructions on standard input. | `gw.instructions`, otherwise unset |
| `-e <list>`, `--excludes <list>` | Supply comma-separated directories or patterns to skip. | `gw.excludes`, otherwise unset |
| `-as <list>`, `--acts <list>` | In Act mode, set comma- or space-separated local directories or HTTP(S) base locations for external Act definitions. Relative directories use the project root and must exist. | `gw.acts`; otherwise no external location, with built-in Acts available on the classpath |
| `-a [name or prompt]`, `--act [name or prompt]` | Enable Act mode and select a named Act or `> task` shorthand; read standard input when supplied without a value. Blank input selects the built-in help Act. | Guidance mode unless this flag is present; `gw.act` alone does not enable Act mode or supply a value to a valueless CLI flag |

These are all nine CLI options; there are no separate CLI flags for request logging, non-recursive traversal, or interactive settings. A missing default configuration file is tolerated; an explicitly selected file that cannot be loaded fails startup. Because configuration loading precedes applying `--projectDir`, use an explicit configuration path when the file is in a different project directory.

### Example

The following command uses a scoped scan, custom project directory, provider/model, concurrency, exclusions, and instructions:

```bash
java -jar gw.jar "glob:**/*.md" --projectDir . --model "OpenAI:gpt-5.1" --threads 4 --excludes "target,.git" --instructions "Keep headings consistent and preserve public links."
```

To select a different properties file, use `--config automation.properties` or place the JVM option before the JAR: `java -Dgw.config=automation.properties -jar gw.jar src`. Keep provider secrets in the environment or a protected configuration source, not in checked-in commands.

For Act mode, run a bundled workflow or an ad-hoc task:

```text
java -jar gw.jar . --act "code-doc"
java -jar gw.jar . --act "> Summarize the project and update its documentation."
```

Use `--acts "acts,shared-acts" --act documentation-update` only after creating those directories and the requested custom Act. The built-in help also illustrates directory and relative-path scans, `"glob:**/*.java"`, and `"regex:^.*/[^/]+\.java$"`. In automation, always supply values for `--instructions` and `--act` if those flags are used, since omitting their values prompts on standard input.

## Resources

- [Machanism platform](https://www.machanism.org/)
- [Ghostwriter source repository](https://github.com/machanism-org/ghostwriter)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Ghostwriter MCP Server](gw-mcp-server/)
- [Guidance-Driven Processing documentation](https://www.machanism.org/guided-file-processing/index.html)
- [Act-Driven Workflows documentation](https://www.machanism.org/act/index.html)
