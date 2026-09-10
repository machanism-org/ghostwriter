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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/ghostwriter/refs/heads/main/bindex.json)

## Introduction

Machai Ghostwriter is an AI-powered agent for maintaining an entire project rather than only the file open in an editor. It can work with source code, tests, documentation, website content, configuration, diagrams, and other relevant project files. Guidance is kept beside the content it governs, making requested changes explicit, reviewable, and repeatable; reusable workflows extend this approach to coordinated repository tasks.

Its conceptual foundations are [Guidance-Driven Processing (GDP)](https://www.machanism.org/guided-file-processing/index.html), which places durable instructions in project artifacts, and [Act-Driven Workflows (ADW)](https://www.machanism.org/act/index.html), which models reusable, multi-step AI workflows. Together, they make it practical to automate documentation maintenance, structured project changes, and CI/CD-friendly processing while retaining project-specific intent.

## Overview

Ghostwriter resolves runtime settings, scans a selected project scope, and routes supported content to format-aware processing. In the default Guidance mode, it discovers embedded `@guidance` directives and processes the files that contain them. In Act mode, it executes a selected prompt workflow, sharing project context across its steps. Configured AI providers can use registered tools for safe project-file operations, approved commands, web and REST access, and workflow control.

The architecture separates command-line startup and configuration from scanning, AI orchestration, guidance handling, workflow execution, and format-specific review. These services are supported by focused tool adapters and provider management, so local project operations and remote AI services can evolve independently. Users invoke the command-line interface; it selects the processing mode and coordinates these services against project content and optional remote resources.

![Ghostwriter component diagram](./images/c4-diagram.png)

## Machai Ghostwriter vs. Other Tools

[OpenHands](https://github.com/All-Hands-AI/OpenHands) is the closest comparable tool because it is an extensible AI software-development agent that can perform repository-level tasks through tools and can be used in automated development workflows. Both tools go beyond inline completion by operating on a project as a whole, integrating with external services, and supporting automation. Ghostwriter is distinguished by its file-embedded GDP guidance and its ADW model, which make instructions and reusable workflow definitions first-class project artifacts.

**Key similarities**

- Both support agentic, multi-file project work rather than only code completion.
- Both can be extended with tools and incorporated into automated engineering workflows.
- Both can use an AI model to inspect project context and make coordinated changes.

**Key differences**

- Ghostwriter processes all relevant project-file types through guidance-bearing artifacts; OpenHands is primarily an autonomous software-development environment and agent platform.
- Ghostwriter keeps durable `@guidance` instructions in the governed file and provides Act definitions for repeatable workflows; OpenHands emphasizes task-driven agent execution.
- Ghostwriter's CLI scans configurable paths and patterns, making narrowly scoped documentation and content processing straightforward in CI/CD jobs.

Tabnine, GitHub Copilot, Claude Code, and Cursor are highly effective developer-assistance tools, but their central experiences are code completion, chat, IDE assistance, or interactive coding agents. Claude Code can perform broad repository tasks, while Copilot and Cursor increasingly support agentic edits; however, Ghostwriter specifically combines project-wide scanning with persistent custom guidance and documentation-oriented processing.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| **Machai Ghostwriter** | Yes — path-scoped scans and Acts | Yes — embedded directives and instructions | Yes — CLI-oriented | Yes — a primary use case across project files |
| OpenHands | Yes — agent tasks and tools | Yes — task prompts and configuration | Yes — automation/deployment integrations | Yes — task-dependent |
| Tabnine | Limited — primarily developer/IDE workflows | Limited — organization and chat context | Limited | Limited |
| GitHub Copilot | Yes — agent features and repository workflows | Yes — repository instructions and prompts | Yes — GitHub ecosystem workflows | Yes — chat/agent-assisted |
| Claude Code | Yes — terminal-based repository tasks | Yes — project instructions and prompts | Yes — scriptable terminal workflow | Yes — agent-assisted |
| Cursor | Yes — interactive multi-file agent edits | Yes — rules and prompts | Limited — primarily IDE-centered | Yes — agent-assisted |

Machai Ghostwriter is unique in treating guidance-tagged project files and Act-driven workflows as the durable control plane for AI automation across code, documentation, websites, and other repository content.

## Key Features

- Scans project directories, individual paths, and `glob:` or `regex:` patterns.
- Processes embedded `@guidance` directives in supported Java, Markdown, PlantUML, HTML, Python, TypeScript, and text artifacts.
- Provides Guidance mode for file-local instructions and Act mode for reusable, episode-driven workflows.
- Supports configurable AI provider/model selection, system instructions, exclusions, project directory, and concurrency.
- Offers registered tools for project files, security-checked commands, web content, REST APIs, project context, and nested workflow execution.
- Resolves local and remote resources and can use shared context between workflow episodes.
- Packages as a Java CLI for local use and repeatable CI/CD automation.

## Getting Started

### Prerequisites

- **Java 8 or newer.** The Maven build sets `maven.compiler.release` to **8**.
- A configured, supported Machai GenAI provider and model, including any required credentials, endpoint, and network access.
- Read/write access to the target project; use network access when the provider, remote Acts, or referenced HTTP(S) resources need it.
- **Maven 3.x** to build from source. Building the delivery pack additionally requires the `MACHANISM_PACK_DIR` environment variable.

## Machai Ghostwriter CLI Pack

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

Download and unpack the delivery pack to run the CLI, or build the project with Maven. See [Bindex Core](https://machai.machanism.org/bindex-core/index.html) for the related indexing component.

### Basic Usage

Run the executable JAR with a path or pattern to scan:

```bash
java -jar gw.jar "glob:**/*.md"
```

The positional `<path>` can be a relative path inside the project, a directory name, a `glob:` pattern such as `glob:**/*.java`, or a `regex:` pattern such as `regex:^.*/[^/]+\.java$`. Absolute paths must remain inside the configured project directory. If no positional path is provided, Ghostwriter uses the configured path or `.`.

### Typical Workflow

1. Configure the AI provider, model, credentials, and project directory.
2. Add precise `@guidance` directives to the files that should govern their own processing, or create/select an Act workflow.
3. Start with a narrow file path or pattern and supply exclusions where needed.
4. Run the CLI, review the resulting changes and command logs, and refine the guidance or Act.
5. Reuse the same configuration and scoped command in CI/CD after validating it locally.

### Java Version and Functional Requirements

Ghostwriter requires Java 8 or later. Functional processing also requires an available GenAI provider/model configuration; project writes require suitable file permissions, and remote providers or resources require the relevant credentials and network connectivity.

## Configuration

Command-line values take precedence over properties loaded from the selected configuration file. Run `java -jar gw.jar --help` to print the complete CLI syntax, descriptions, and examples.

### Command-Line Options

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Print help and exit without processing. | Disabled |
| `-d <dir>`, `--projectDir <dir>` | Set the project directory used for processing. | Configured `projectDir`, otherwise the current user directory |
| `-c <file>`, `--config <file>` | Select a configuration properties file. | `gw.properties` in the project directory, unless configured through the system property |
| `-t <n>`, `--threads <n>` | Set concurrent processing threads; higher values can improve throughput but increase resource and provider use. | Configured `threads`, otherwise processor default |
| `-m <provider:model>`, `--model <provider:model>` | Select the GenAI provider and model, for example `OpenAI:gpt-5.1`. | Configured model |
| `-i [text]`, `--instructions [text]` | Set system instructions. When used without text, prompt for the instructions on standard input. | Configured `instructions` |
| `-e <list>`, `--excludes <list>` | Supply comma-separated directories or patterns to skip. | Configured exclusions |
| `-as <dir>`, `--acts <dir>` | Set the directory containing predefined Act prompt files. | Configured Acts location |
| `-a [name]`, `--act [name]` | Enable interactive Act mode and optionally select the Act; prompts for a name when supplied without one. | Guidance mode; configured Act when applicable |

### Example

The following command uses a scoped scan, custom project directory, provider/model, concurrency, exclusions, and instructions:

```bash
java -jar gw.jar "glob:**/*.md" --projectDir . --model "OpenAI:gpt-5.1" --threads 4 --excludes "target,.git" --instructions "Keep headings consistent and preserve public links."
```

For Act mode, use `--act` with an optional Act name, for example `java -jar gw.jar . --act documentation-update`. The built-in help also shows raw directory, relative-path, glob, and regular-expression scan examples.

## Resources

- [Machanism platform](https://www.machanism.org/)
- [Ghostwriter source repository](https://github.com/machanism-org/ghostwriter)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Ghostwriter MCP Server](gw-mcp-server/)
- [Guidance-Driven Processing documentation](https://www.machanism.org/guided-file-processing/index.html)
- [Act-Driven Workflows documentation](https://www.machanism.org/act/index.html)
