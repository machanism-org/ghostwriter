<!-- @guidance: >>> ${guidances}/readme-content.md
# Additional Content

## Usage  
- Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download) to the installation section.

## Resources
- [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server)
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/ghostwriter/refs/heads/main/bindex.json)

## Cloning and Getting Started

To clone and set up this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/machanism-org/ghostwriter.git
   cd ghostwriter
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

## Introduction

Ghostwriter is an AI-powered documentation and project-maintenance agent. It scans source code, documentation, website content, configuration, diagrams, and other relevant project files, then uses file-local guidance and AI-assisted processing to make coordinated, reviewable changes.

Its approach is founded on [Guidance-Driven Processing (GDP)](https://www.machanism.org/guided-file-processing/index.html), which keeps durable instructions beside the content they govern, and [Act-Driven Workflows (ADW)](https://www.machanism.org/act/index.html), which defines reusable multi-step workflows. Together, these models support repeatable documentation maintenance and project-wide automation while preserving project-specific intent.

## Overview

Ghostwriter resolves runtime configuration, scans a selected project scope, and routes supported content for format-aware processing. In Guidance mode, it discovers embedded `@guidance` directives and processes the files containing them. In Act mode, it executes a selected workflow and shares project context between its steps. Configured AI providers can use registered tools for safe project-file operations, approved commands, web and REST access, and workflow control.

The architecture separates command-line startup and configuration from file scanning, AI orchestration, guidance handling, workflow execution, and format-specific review. Tool adapters provide file, command, web, workflow, guidance, and project-context capabilities, while provider management connects the configured AI service to those enabled tools.

## Project Structure

Ghostwriter is organized around a command-line entry point that loads runtime configuration and selects either Guidance or Act processing. A shared file-processing core traverses the selected project scope, applies path matching and exclusions, and delegates work to AI orchestration. Guidance processing discovers directives and invokes format-aware reviewers, while Act processing loads reusable workflows, coordinates episodes, and shares project context between steps.

Provider management connects the processing core to the configured GenAI service. Registered tool adapters expose controlled operations for project files, approved commands, web and REST resources, workflow execution, guidance processing, and shared context. This separation keeps local project access, workflow control, content review, and remote model interaction independently maintainable.

## Key Features

- Scans project directories, individual paths, and `glob:` or `regex:` patterns.
- Processes embedded `@guidance` directives in supported Java, Markdown, PlantUML, HTML, Python, TypeScript, and text artifacts.
- Provides Guidance mode for file-local instructions and Act mode for reusable, episode-driven workflows.
- Supports configurable AI provider and model selection, system instructions, exclusions, project directory, and concurrency.
- Offers tools for project files, security-checked commands, web content, REST APIs, project context, and nested workflow execution.
- Packages as a Java CLI for local use and repeatable CI/CD automation.

## Installation

Build from source with Maven, or download and unpack the CLI delivery pack:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

The Maven build targets **Java 8** (`maven.compiler.release` is `8`). Building the delivery pack additionally requires Maven 3.x and the `MACHANISM_PACK_DIR` environment variable. Running Ghostwriter requires a configured supported Machai GenAI provider and model, relevant credentials, and read/write access to the target project. Network access is needed when using remote providers or resources.

## Usage

Run the executable JAR with a path or pattern to scan:

```bash
java -jar gw.jar "glob:**/*.md"
```

The positional path can be a relative path inside the project, a directory, a `glob:` pattern such as `glob:**/*.java`, or a `regex:` pattern such as `regex:^.*/[^/]+\\.java$`. If no path is supplied, Ghostwriter uses the configured path or the current directory.

### Typical Workflow

1. Configure the AI provider, model, credentials, and project directory.
2. Add precise `@guidance` directives to governed files, or create and select an Act workflow.
3. Run a narrow path or pattern first and supply exclusions where necessary.
4. Review the changes and command logs, then refine the guidance or workflow.
5. Reuse the scoped command in CI/CD after validating it locally.

## Configuration

Command-line values take precedence over properties loaded from the selected configuration file. Run `java -jar gw.jar --help` for the complete syntax, descriptions, and examples.

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Print help and exit without processing. | Disabled |
| `-d <dir>`, `--projectDir <dir>` | Set the project directory used for processing. | Configured `projectDir`, otherwise the current user directory |
| `-c <file>`, `--config <file>` | Select a configuration properties file. | `gw.properties` in the project directory unless set through a system property |
| `-t <n>`, `--threads <n>` | Set concurrent processing threads. | Configured `threads`, otherwise the processor default |
| `-m <provider:model>`, `--model <provider:model>` | Select the GenAI provider and model. | Configured model |
| `-i [text]`, `--instructions [text]` | Set system instructions; without text, prompt on standard input. | Configured `instructions` |
| `-e <list>`, `--excludes <list>` | Supply comma-separated directories or patterns to skip. | Configured exclusions |
| `-as <dir>`, `--acts <dir>` | Set the directory containing predefined Act prompt files. | Configured Acts location |
| `-a [name]`, `--act [name]` | Enable Act mode and optionally select an Act. | Guidance mode; configured Act when applicable |

For example:

```bash
java -jar gw.jar "glob:**/*.md" --projectDir . --model "OpenAI:gpt-5.1" --threads 4 --excludes "target,.git" --instructions "Keep headings consistent and preserve public links."
```

Use `--act` with an optional Act name for workflow processing, for example `java -jar gw.jar . --act documentation-update`.

## Resources

- [Machanism platform](https://www.machanism.org/)
- [Ghostwriter source repository](https://github.com/machanism-org/ghostwriter)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server)
- [Guidance-Driven Processing documentation](https://www.machanism.org/guided-file-processing/index.html)
- [Act-Driven Workflows documentation](https://www.machanism.org/act/index.html)
