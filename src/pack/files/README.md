# Ghostwriter CLI

## Application overview

Machai Ghostwriter is a Java command-line AI agent for project-wide file processing. It scans selected files, directories, glob patterns, or regular-expression paths and applies GenAI-assisted guidance to source code, tests, documentation, website content, configuration, diagrams, and other project artifacts. Guidance mode is the default; Act mode executes a reusable prompt workflow against a project.

Typical uses include maintaining documentation, applying repeatable repository-wide changes, processing selected file types in batch, and running controlled updates locally or in CI/CD. Review generated changes before committing them.

Supported provider/model forms represented by this pack include:

- **CodeMie**, for example `CodeMie:gpt-5-2-2025-12-11`.
- **OpenAI-compatible services**, for example `OpenAI:gpt-5.1`, optionally using `OPENAI_BASE_URL` for a compatible endpoint.

Provider availability, authentication, and endpoint behavior are supplied by the bundled GenAI client.

## Installation

### Prerequisites

- Java 8 or later. The Maven project sets `maven.compiler.release` to `8`.
- A reachable GenAI provider/model, valid credentials, and any required endpoint configuration.
- Read/write access to the project being processed.
- Either the delivery pack or a source checkout with Maven and access to the dependency repositories.
- Version control is recommended so generated edits can be reviewed and reverted.

### Use the delivery pack

1. Download the [Ghostwriter CLI pack](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
2. Extract it to a local directory.
3. Copy or edit the supplied `gw.properties` file. Ghostwriter loads `gw.properties` by default.
4. Configure the provider/model and credentials without committing secrets.
5. Verify the installation:

```text
java -jar gw.jar --help
```

The supplied properties file selects `CodeMie:gpt-5-2-2025-12-11` and contains commented examples for CodeMie and OpenAI-compatible credentials.

### Build from source

From the repository checkout:

```sh
mvn clean install
```

The `pack` profile builds `target/gw.jar` and the delivery-pack output. It requires `MACHANISM_PACK_DIR`:

```sh
MACHANISM_PACK_DIR=/path/to/pack mvn -Ppack clean install
```

On Windows:

```bat
set "MACHANISM_PACK_DIR=C:\path\to\pack"
mvn -Ppack clean install
```

## How to run

```text
java -jar gw.jar <path> [options]
```

`<path>` may be a relative file or directory, a path inside the project root, a glob such as `glob:**/*.java`, or a regex such as `regex:^.*/[^/]+\\.java$`. Multiple positional paths are accepted. If no path is supplied, Ghostwriter uses `gw.path`, then `.`. Absolute paths must be inside the configured project directory.

There is no separate `--root` option. Use `-d`/`--projectDir` or `project.dir` to set the project root; relative scan paths are resolved against it.

### Command-line options

These options are defined by `org.machanism.machai.gw.processor.Ghostwriter`. Explicit command-line values take precedence over configuration values.

| Option | Description | Default or context |
|---|---|---|
| `-h`, `--help` | Print usage, options, and examples, then exit. | Disabled. |
| `-d`, `--projectDir <path>` | Set the project root used to resolve and scan paths. | `project.dir`; otherwise the current user directory. |
| `-c`, `--config <file>` | Select the Java properties configuration file. | `gw.properties`; `gw.config` can select another file. Relative paths use the initial project directory. |
| `-t`, `--threads <n>` | Set the number of concurrent processing threads. | `gw.threads`; otherwise the processor default. Must be a valid positive integer. |
| `-m`, `--model <provider:model>` | Select the GenAI provider and model. | `gw.model`; otherwise unset. |
| `-i`, `--instructions [text]` | Set system instructions. Without a value, read them from standard input. | `gw.instructions`; otherwise unset. |
| `-e`, `--excludes <csv>` | Set comma-separated files, directories, or patterns to skip. | `gw.excludes`; otherwise no configured exclusions. |
| `-as`, `--acts <path>` | Set the location of predefined Act files; local paths and supported HTTP(S) locations may be used. | `gw.acts`; otherwise the Act processor default. |
| `-a`, `--act [name or prompt]` | Enable Act mode and select an Act or prompt. Without a value, read it from standard input. | Guidance mode unless present. |

### Configuration properties

Put these properties in the selected Java properties file. `gw.config` is a Java system property used to select the file, not a normal property loaded from it.

| Property | Description | Default or context |
|---|---|---|
| `project.dir` | Base project directory (project root). | Current user directory when absent; overridden by `-d`. |
| `gw.config` | Java system property naming the configuration file. | `gw.properties` when absent; `-c` takes precedence. |
| `gw.model` | GenAI provider/model identifier. | Unset unless configured; overridden by `-m`. |
| `gw.instructions` | Default system instructions. | Unset unless configured; overridden by `-i`. |
| `gw.excludes` | Comma-separated exclusions. | Unset unless configured; overridden by `-e`. |
| `gw.acts` | Location of external Act definitions. | Processor default unless configured; overridden by `-as`. |
| `gw.act` | Default Act name or prompt read when Act mode is selected. | Unset unless configured; does not itself enable Act mode. |
| `gw.threads` | Concurrent processing thread count. | Processor default unless configured; overridden by `-t`. |
| `gw.path` | Default file, directory, glob, or regex scan target. | `.` when absent; positional paths take precedence. |

Example `gw.properties`:

```properties
project.dir=.
gw.model=CodeMie:gpt-5-2-2025-12-11
gw.threads=4
gw.excludes=.git,target,node_modules
gw.path=src
gw.instructions=Keep headings consistent and preserve public links.
```

### Credentials and system properties

Use process environment variables for provider credentials, as required by the GenAI client. Do not commit secrets:

- CodeMie: `GENAI_USERNAME` and `GENAI_PASSWORD`.
- OpenAI-compatible services: `OPENAI_API_KEY` and, for a non-default endpoint, `OPENAI_BASE_URL`.

Unix:

```sh
export OPENAI_API_KEY="your-api-key"
export OPENAI_BASE_URL="https://your-openai-compatible-endpoint"
java -Dgw.config=production.properties -jar gw.jar src
```

Windows:

```bat
set "GENAI_USERNAME=your_codemie_username"
set "GENAI_PASSWORD=your_codemie_password"
java -Dgw.config=production.properties -jar gw.jar src
```

Only `gw.config` is read by `Ghostwriter` as a Java system-property override. The other runtime settings belong in the properties file or on the command line.

### Unix examples

```sh
# Guidance mode with root, model, instructions, exclusions, and concurrency
java -jar gw.jar "glob:**/*.md" -d /work/my-project \
  -m OpenAI:gpt-5.1 -t 4 -e ".git,target" \
  -i "Keep headings consistent and preserve public links."

# Prompt for instructions
java -jar gw.jar src --instructions

# Run an Act from a local directory
java -jar gw.jar . --acts ./acts --act "Summarize the repository"
```

### Windows examples

```bat
rem Guidance mode with root, model, instructions, exclusions, and concurrency
java -jar gw.jar "glob:**/*.md" -d C:\work\my-project -m OpenAI:gpt-5.1 -t 4 -e ".git,target" -i "Keep headings consistent."

rem Prompt for instructions
java -jar gw.jar src --instructions

rem Run an Act from a local directory
java -jar gw.jar . --acts .\acts --act "Summarize the repository"
```

A trailing backslash in interactive input continues the value on the next line. Run `java -jar gw.jar --help` for the built-in path syntax and examples.

## Troubleshooting and support

- **Authentication or provider errors:** verify the provider/model spelling, credentials, endpoint, network access, account permissions, and quota.
- **Missing files or unexpected scans:** set `-d` explicitly, verify the positional path or `gw.path`, check glob/regex syntax, and inspect `gw.excludes`. Absolute paths must remain below the project directory.
- **Configuration not loaded:** check the `-c` path or `-Dgw.config` value. A missing default `gw.properties` is tolerated; an explicitly selected file that cannot be loaded fails startup.
- **Act not found:** verify `--acts`/`gw.acts` and the requested `--act` value.
- **Thread errors:** ensure `--threads`/`gw.threads` is a valid positive integer.
- **Logs and debug output:** lifecycle, configuration, scan, failure, and usage messages use SLF4J and the packaged logging backend. Configure that backend for DEBUG or TRACE for `org.machanism.machai.gw`; enable provider request logging only when safe. The CLI does not define a fixed log-file location.
- **Review changes:** inspect `git diff` or the equivalent before publishing results.

## Documentation and contact

- [Machanism platform](https://www.machanism.org/)
- [Ghostwriter source repository and support](https://github.com/machanism-org/ghostwriter)
- [Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Guidance-Driven Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Act-Driven Workflows](https://www.machanism.org/act/index.html)
- [Bindex Core](https://machai.machanism.org/bindex-core/index.html)
- [CLI download](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

Ghostwriter is released under the Apache License 2.0. For provider-specific authentication, consult the selected GenAI client/provider documentation.
