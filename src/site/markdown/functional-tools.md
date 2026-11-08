<!-- @guidance: 
Create or update the `Function Tolls` page:
- Analyze classes in the folder: `/src/main/java/org/machanism/machai/gw/tools` and use this information to create the page content but do not mentionad this as a package details.
- If the function tool class is annotated with the `@SupportedFor` annotation, specify this in the description of the function tool methods.
- Write a general description of the each functional tool.
- Describe a feature and input parameters.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Function Tools

Function tools provide Ghostwriter workflows with project-aware actions for running Acts, controlling episodes, executing commands, changing files, processing guidance tags, sharing project context, and accessing web resources. Each tool below describes its purpose, appropriate use, and caller-supplied inputs. Runtime-managed values such as the project directory and configuration are omitted from parameter lists.

## Act tools

Act tools inspect and run named, reusable workflows. `ActFunctionTools` is annotated with `@SupportedFor(excludes = GuidanceProcessor.class)`; its tools are available except when the active processor is a `GuidanceProcessor`.

### `get_act_details`
Loads the instructions, input template, and configuration options for a named Act. It searches both project-defined and built-in definitions, making it useful for inspecting an Act before running or editing it.

**Availability:** Supported by `ActFunctionTools` for processors other than `GuidanceProcessor`.

**Input:** `name` — Act name.

### `perform_act`
Runs a named Act in the current project. It can return the completed result synchronously or start background processing and return a process identifier. Property overrides are resolved and applied before execution.

**Availability:** Supported by `ActFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `name` — Act name; `properties` — optional configuration overrides; `async` — whether to run in the background (default `true`).

### `get_act_result`
Polls a background Act started by `perform_act`. It returns `status: done` and the result when serialization is complete, or `status: processing` when reading encounters an incomplete serialized result. A missing result file raises a file-not-found error rather than returning a processing status.

**Availability:** Supported by `ActFunctionTools` for processors other than `GuidanceProcessor`.

**Input:** `process_id` — identifier returned by `perform_act`.

## Episode control tools

`ActSpecFunctionTools` is annotated with `@SupportedFor({ ActProcessor.class })`; these tools are intended for `ActProcessor` workflows. They signal control flow by throwing an internal workflow exception rather than returning a normal result.

### `move_to_episode`
Explicitly jumps to an episode identified by an ID or name. Use it only when the user explicitly requests navigation to a particular episode; normal progression to the next episode is automatic.

**Availability:** Supported by `ActSpecFunctionTools` for `ActProcessor` workflows.

**Inputs:** `id` — integer episode ID; `name` — episode name.

### `repeate_episode`
Requests another pass through the current episode while preserving workflow context. It is useful after validation failure or when another input pass is required.

**Availability:** Supported by `ActSpecFunctionTools` for `ActProcessor` workflows.

**Input:** `message` — optional message emitted before repetition; defaults to an empty string.

## Command tools

Command tools execute approved commands in a project-relative working directory and retain output for later inspection. `CommandFunctionTools` has no `@SupportedFor` annotation; command security checks still determine whether a command may run.

### `run_sys_command`
Runs a command with controlled environment variables, working-directory validation, output capture, timeout handling, and character-set decoding. It returns an execution report and raises an error result when the command fails.

**Inputs:** `command` — command line; `env` — optional child-process environment variables; `dir` — relative working directory, default `.`; `tail_result_size` — output tail length, default `1024`; `charset` — output encoding, default `UTF-8`.

### `get_log_chunk`
Retrieves the portion of a saved command log immediately before the currently displayed tail. Use it to page backward through long output.

**Inputs:** `command_log_id` — command log identifier; `tail_result_size` — requested preceding fragment length, default `1024`; `current_tail_offset` — start offset of the current tail; `charset` — log encoding, default `UTF-8`.

### `get_log_matches`
Searches a saved command log using a Java regular expression and returns every match with its text, line number, and character positions.

**Inputs:** `command_log_id` — command log identifier; `regexp` — Java regular expression; `charset` — log encoding, default `UTF-8`.

## Execution control tools

`CommandSpecFunctionTools` is annotated with `@SupportedFor({ AIFileProcessor.class })`; these controls are intended for `AIFileProcessor` workflows.

### `terminate_execution`
Requests application termination by raising a controlled termination signal. Use it only when the user explicitly requests application termination; do not use it merely because a task completed.

**Availability:** Supported by `CommandSpecFunctionTools` for `AIFileProcessor` workflows.

**Inputs:** `message` — optional host-facing message, default `Execution terminated by function tool.`; `exit_code` — exit code, default `0`.

### `end_task`
Completes the current task without shutting down the application, allowing the host to accept later tasks. Use it when the user requests ending the task, particularly to conclude an interactive session.

**Availability:** Supported by `CommandSpecFunctionTools` for `AIFileProcessor` workflows.

**Input:** `message` — optional completion message, default `Execution terminated by function tool.`.

## File tools

`FileFunctionTools` has no `@SupportedFor` annotation. Its operations resolve paths beneath the project root and support text encoding selection; use them for controlled project file inspection and updates.

### `list_files_in_directory`
Recursively lists files and directories below a directory, returning separate `directories` and `files` lists with project-relative paths.

**Input:** `path` — directory to scan, default `.`.

### `get_recursive_file_list`
Returns project-relative files recursively below a directory. It returns a no-files message when appropriate and rejects results exceeding the configured limit.

**Inputs:** `path` — root directory, default empty; `max_count` — maximum file count, default `50`.

### `get_recursive_folder_list`
Returns only subdirectories recursively below a root, excluding the root itself and all files.

**Inputs:** `dir` — root directory, default empty; `max_count` — maximum folder count, default `50`.

### `write_file`
Creates or replaces a text file and creates missing parent directories for new files.

**Inputs:** `path` — destination file; `text` — complete content; `charset` — encoding, default `UTF-8`.

### `read_file`
Reads a text file under the project root and enforces a maximum returned character length.

**Inputs:** `path` — file to read; `charset` — encoding, default `UTF-8`; `max_file_size` — maximum character count, default `100000`.

### `apply_patch_to_file`
Applies either a standard unified diff or the supported simplified search-and-replace patch to a project file. Use it for focused edits rather than replacing an entire file.

**Inputs:** `file` — target file; `patch` — patch text; `charset` — encoding, default `UTF-8`.

## Guidance tools

`GuidanceFunctionTools` is annotated with `@SupportedFor(excludes = GuidanceProcessor.class)`; its tools are intended for supported processors other than `GuidanceProcessor`. These operations discover files containing `@guidance` tags, process them with the configured model, and retrieve asynchronous reports.

### `get_guidance_tagged_files`
Scans matching files and groups files containing guidance tags by project directory. Use it only as the first step when the user explicitly requests guidance-tag processing, not for general searching, test fixing, or ordinary file inspection. This is a discovery operation and does not apply the instructions; files outside the supplied pattern are not inspected.

**Availability:** Supported by `GuidanceFunctionTools` for processors other than `GuidanceProcessor`.

**Input:** `path` — raw path, `glob:` pattern, or `regex:` pattern, default `glob:**/*.*`.

### `process_guidance_tagged_files`
Applies guidance processing to matching files using optional global instructions and configured properties. It can block for a report or run in the background and return a process ID.

**Availability:** Supported by `GuidanceFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `instructions` — optional global instructions that supplement or take precedence over embedded directives; `properties` — optional processing overrides; `path` — raw scan path, `glob:` pattern, or `regex:` pattern, default `.`; `async` — background mode, default `true`.

### `get_guidance_tagged_files_process_result`
Retrieves an asynchronous guidance-processing report. A missing report file returns `status: processing`; a successfully read report returns `status: done` with the result. An unreadable or partially written report raises an I/O error. Background failures are logged and can leave the report unavailable, so a processing status alone does not guarantee the job is still running.

**Availability:** Supported by `GuidanceFunctionTools` for processors other than `GuidanceProcessor`.

**Input:** `process_id` — identifier returned by `process_guidance_tagged_files`.

## Project context tools

`ProjectContextFunctionTools` is annotated with `@SupportedFor(excludes = GuidanceProcessor.class)`; its tools are intended for supported processors other than `GuidanceProcessor`. They maintain project-scoped state shared between Acts, episodes, and prompt templates, not operating-system environment variables. Each operation can instead target an application-wide context shared across projects by setting `global` to `true`.

### `put_project_context_variable`
Creates or replaces a named project context value.

**Availability:** Supported by `ProjectContextFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `name` — variable name; `value` — value to store; `global` — store in the application-wide context shared across projects instead of the current project's context (default `false`).

### `get_project_context_variables`
Returns a map containing the requested context names and their stored values. Missing names have null values; no context for the project is an error.

**Availability:** Supported by `ProjectContextFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `names` — list of variable names; `global` — read from the application-wide context shared across projects instead of the current project's context (default `false`).

### `push_project_context_variable`
Appends a value to a context variable, creating a list when needed or converting an existing string into a list.

**Availability:** Supported by `ProjectContextFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `name` — variable name; `value` — value to append; `global` — append to the application-wide context shared across projects instead of the current project's context (default `false`).

### `pop_project_context_variable`
Removes and returns a context value. Lists support LIFO or FIFO removal, and are simplified or removed when they become shorter.

**Availability:** Supported by `ProjectContextFunctionTools` for processors other than `GuidanceProcessor`.

**Inputs:** `name` — variable name; `mode` — `LIFO` or `FIFO` (default behavior is LIFO); `global` — remove from the application-wide context shared across projects instead of the current project's context (default `false`).

## Web tools

`WebFunctionTools` has no `@SupportedFor` annotation. It provides HTTP GET and general REST access, with optional headers, timeouts, response encodings, CSS selection, plain-text rendering, Basic authentication through URL user information, and runtime property substitution. Relative `file:` URLs resolve against the project directory; absolute file paths are also accepted.

### `get_web_content`
Fetches an HTTP(S) page with GET or reads a `file:` URL. Relative file paths resolve against the project directory, while absolute file paths are also accepted. HTTP responses include a status line before the body unless HTML selection or text rendering changes the output. It can select matching CSS elements and optionally render the response as plain text; use `text_only: false` and omit `selector` to retain the unfiltered content.

**Inputs:** `url` — web or file URL; `headers` — optional request headers; `timeout` — milliseconds, default `0`; `charset` — response encoding, default `UTF-8`; `text_only` — strip HTML, default `false`; `selector` — optional CSS selector.

### `call_rest_api`
Sends a REST request with a configurable method, headers, body, timeout, and response encoding. A nonempty request body is sent only for POST, PUT, or PATCH, using the selected character set. The method must be supported by the underlying Java HTTP connection; unsupported methods can raise an error. The response includes its HTTP status line followed by the body, or a response-code summary when no response stream is available.

**Inputs:** `url` — HTTP(S) endpoint; `method` — HTTP method, default `GET`; `headers` — optional request headers; `body` — request body, default empty; `timeout` — connection and read timeout in milliseconds, default `0` (connection defaults); `charset` — response decoding and request-body encoding, default `UTF-8`.
