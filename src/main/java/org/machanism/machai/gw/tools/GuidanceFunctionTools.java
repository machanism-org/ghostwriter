package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.LayeredConfigurator;
import org.machanism.macha.core.commons.configurator.MutableConfigurator;
import org.machanism.macha.core.commons.configurator.Substitutor;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.GWConstants;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Provides function tools for discovering and processing files with guidance
 * tags in project directories.
 * <p>
 * This class registers tools for:
 * <ul>
 * <li>Scanning project directories to find files annotated with guidance
 * tags</li>
 * <li>Processing those files using a configured model, either synchronously or
 * asynchronously</li>
 * <li>Retrieving the results of asynchronous processing by process ID</li>
 * <li>Supplying prompt templates for guidance tag processing</li>
 * </ul>
 * </p>
 * <p>
 * This implementation integrates with the {@link Genai} provider and supports
 * both custom and built-in project workflows. It manages asynchronous execution
 * and result retrieval using temporary files and process IDs. Methods in this
 * class are typically invoked by an AI provider or workflow engine to enable
 * dynamic, tool-augmented project automation involving guidance tags.
 * Asynchronous reports are serialized below the application temporary directory
 * and can be retrieved with the process identifier returned when processing
 * starts.
 * </p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor(excludes = GuidanceProcessor.class)
public class GuidanceFunctionTools implements FunctionTools {

	/**
	 * Creates a guidance function-tools provider using the default prompt resource
	 * bundle.
	 */
	public GuidanceFunctionTools() {
		// Default construction initializes instance fields.
	}

	/** Logger used to report asynchronous guidance-processing failures. */
	private static final Logger logger = LoggerFactory.getLogger(GuidanceFunctionTools.class);

	/**
	 * Directory below the runtime temporary directory that stores guidance results.
	 */
	private static final String GUIDANCE_FOLDER = "guidance";
	/** Response-map key for an asynchronous guidance execution identifier. */
	private static final String PROCESS_ID_KEY = "process_id";
	/** Response-map key for an asynchronous guidance execution status. */
	private static final String STATUS_KEY = "status";

	/**
	 * Resource bundle that supplies prompt templates exposed through {@link Prompt}
	 * methods in this tool provider.
	 */
	final ResourceBundle mcpPromptBundle = ResourceBundle.getBundle("mcp-prompts");

	/**
	 * Scans the specified directory and its subdirectories for files annotated with
	 * guidance tags, returning a mapping of project directories to the files that
	 * contain such tags.
	 * <p>
	 * The scan is performed relative to the provided root directory and can be
	 * filtered using a path or pattern (such as glob or regex). Each discovered
	 * file with a guidance tag is grouped under its corresponding project directory
	 * in the returned map.
	 * </p>
	 *
	 * @param rootDir      The absolute path to the root project directory or a
	 *                     folder containing multiple projects. All scanning
	 *                     operations are performed relative to this directory.
	 * @param path         Specifies the scanning path or pattern. Use a relative
	 *                     path with respect to the current project directory. If an
	 *                     absolute path is provided, it must be located within the
	 *                     root project directory. Supported patterns: raw directory
	 *                     names, glob patterns (e.g., "glob:*.java"), or regex
	 *                     patterns (e.g., "regex:^.java$"). Default: "glob:*.*"
	 * @param projectDir   The project directory to use as the working directory for
	 *                     scanning operations.
	 * @param configurator The configuration object.
	 * @return A map where each key is a project directory and each value is a list
	 *         of files with guidance tags found in that directory.
	 * @throws IOException if an I/O error occurs during scanning.
	 */
	@Tool(name = "get_guidance_tagged_files", description = "Scans and groups files containing @guidance tags by project directory. "
			+ "Not a general search tool. Use ONLY as the first step when the user explicitly requests "
			+ "guidance-tag processing (e.g., 'process guidance tags'). Do not invoke for normal tasks, "
			+ "test fixing, or file inspection.")
	public Map<File, List<File>> getGuidanceTaggedFiles(
			@Param(name = "path", description = "The scanning path or pattern used to select candidate files to "
					+ "inspect for @guidance tags. Provide a path relative to 'project_dir'. If an absolute path is "
					+ "given, it must still fall within 'root_dir'. Supported forms: a plain relative directory "
					+ "name, a glob pattern (e.g., \"glob:**/*.java\"), or a regex pattern "
					+ "(e.g., \"regex:^.*/[^/]+\\.java$\"). Only files matching this pattern are scanned for "
					+ "@guidance tags — files outside the pattern are skipped entirely, regardless of their content.", defaultValue = "glob:**/*.*") String path,
			File projectDir,
			Configurator configurator)
			throws IOException {
		Map<File, List<File>> map = new HashMap<>();

		String model = configurator.get(GWConstants.MODEL_PROP_NAME, null);
		AIFileProcessor processor = new GuidanceProcessor(projectDir, model, configurator) {
			/**
			 * Records a guidance-tagged file under the directory of its project rather than
			 * applying its guidance instructions.
			 *
			 * @param projectLayout layout that identifies the file's project
			 * @param file          guidance-tagged file found during the scan
			 * @param instructions  extracted guidance instructions, which are not processed
			 *                      by this discovery-only implementation
			 * @param prompts       optional prompts associated with the file, which are not
			 *                      used by this discovery-only implementation
			 * @return {@code null}, because discovery produces no processed-file result
			 */
			@Override
			protected String process(ProjectLayout projectLayout, File file, String instructions, String... prompts) {
				map.computeIfAbsent(projectLayout.getProjectDir(), k -> new ArrayList<>()).add(file);
				return null;
			}
		};

		processor.scanDocuments(projectDir, path);
		return map;
	}

	/**
	 * Runs guidance processing in the background and persists its report.
	 * <p>
	 * Any failure is logged because this method runs outside the caller's execution
	 * context; callers observe an unavailable result until a report is written.
	 * </p>
	 *
	 * @param processor  configured guidance processor
	 * @param projectDir project directory to scan
	 * @param path       scan path or pattern
	 * @param tempFile   file that receives the serialized report
	 */
	private void saveGuidanceResult(GuidanceProcessor processor, File projectDir, String path, File tempFile) {
		try {
			processor.scanDocuments(projectDir, path);
			writeGuidanceResult(tempFile, processor.getReport());
		} catch (Exception ex) {
			logger.error("Error during background guidance tag file processing. Temp file: '{}'",
					tempFile.getAbsolutePath(), ex);
		}
	}

	/**
	 * Serializes a guidance-processing report to its temporary result file.
	 *
	 * @param tempFile destination temporary file; its parent directory must exist
	 * @param result   report to serialize, including the outcome for every
	 *                 processed file
	 * @throws IOException if the report cannot be written
	 */
	private void writeGuidanceResult(File tempFile, List<Map<String, Object>> result) throws IOException {
		// Sonar java:S2095: always close the serialized result stream.
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(tempFile))) {
			output.writeObject(result);
		}
	}

	/**
	 * Asynchronously processes files with guidance tags using the configured model.
	 * <p>
	 * Scans the files in the specified {@code project_dir} (and optionally matching
	 * the given {@code path} pattern) and applies guidance processing to each file
	 * found. The processing is performed in a background thread. The method returns
	 * immediately with a response containing a unique process ID and a status of
	 * "processing". The actual result is serialized to a temporary file for later
	 * retrieval using the process ID.
	 * </p>
	 *
	 * @param projectDir The project directory in which to scan for files.
	 * @param properties Optional map of Act properties, such as configuration
	 *                   overrides or parameters for the guidance processing. If
	 *                   {@code null}, only the main configuration is used.
	 * @param path       Specifies the scanning path or pattern. Use a relative path
	 *                   with respect to the current project directory. If an
	 *                   absolute path is provided, it must be located within the
	 *                   root project directory. Supported patterns: raw directory
	 *                   names, glob patterns (e.g., "glob:**.java"), or regex
	 *                   patterns (e.g., "regex:^.[^/]+\\.java$"). Default:
	 *                   "${project_dir}".
	 * @param config     The configuration object for property resolution and
	 *                   default values.
	 * @return In asynchronous mode, a map containing the unique {@code process_id}
	 *         and a {@code status} of {@code "processing"}; in synchronous mode,
	 *         the complete guidance-processing report.
	 * @throws IOException If there is an error scanning files or initializing the
	 *                     processing configuration.
	 */
	@Tool(name = "process_guidance_tagged_files", description = "Scans files for guidance-tag directives (e.g., `@guidance`) "
			+ "relative to project_dir and applies the configured AI model. Uses model from properties or project default, "
			+ "applies execution properties with placeholder resolution, and supports synchronous blocking "
			+ "or asynchronous execution (returning a process_id for background status tracking).")
	public Object processGuidanceTagFiles(
			@Param(name = "instructions", description = "Optional global guidance instructions or prompt overrides applied during file processing. "
					+ "If provided, these instructions take precedence or supplement the directives embedded within the scanned files.", defaultValue = Param.NULL) String instructions,
			@Param(name = "properties", description = "Guidance processing properties.", defaultValue = Param.NULL) Map<String, String> properties,
			@Param(name = "path", description = "Specifies the scanning path or pattern used to locate files to process. "
					+ "Use a relative path with respect to the current project directory. "
					+ "If an absolute path is provided, it must be located within the root project directory. "
					+ "Supported patterns: raw directory names, glob patterns (e.g., \"glob:**/*.java\"), or regex "
					+ "patterns (e.g., \"regex:^.*/[^/]+\\.java$\"). Defaults to the project directory itself, meaning "
					+ "the entire project is scanned.", defaultValue = ".") String path,
			@Param(name = "async", description = "Controls the execution mode. If true, processing runs in the "
					+ "background and the tool immediately returns a `process_id` and `status` for later polling — "
					+ "useful for MCP server execution or long-running scans that shouldn't block the caller. "
					+ "If false, the tool blocks until processing completes and returns the full report directly.", defaultValue = "true") boolean async,
			File projectDir,
			Configurator config)
			throws IOException {

		MutableConfigurator configurator = new LayeredConfigurator(config);

		String model = null;
		if (properties != null) {
			for (Map.Entry<String, String> e : properties.entrySet()) {
				String value = Substitutor.replace(e.getValue(), configurator);
				configurator.set(e.getKey(), value);
			}
			model = properties.get(GWConstants.MODEL_PROP_NAME);
		}

		if (model == null) {
			model = configurator.get(GWConstants.MODEL_PROP_NAME);
		}

		final GuidanceProcessor processor = new GuidanceProcessor(projectDir, model, configurator);
		processor.setInstructions(instructions);

		if (async) {
			final String processId = UUID.randomUUID().toString();
			final String tempDir = ProjectLayout.getTempDir();
			final File tempFile = new File(new File(tempDir, GUIDANCE_FOLDER), processId + ".tmp");
			tempFile.getParentFile().mkdirs();

			// Sonar java:S2095: a dedicated thread avoids an ExecutorService lifecycle
			// leak.
			Thread backgroundThread = new Thread(
					() -> saveGuidanceResult(processor, projectDir, path, tempFile),
					"guidance-processing-" + processId);
			backgroundThread.start();

			Map<String, Object> response = new HashMap<>();
			response.put(PROCESS_ID_KEY, processId);
			response.put(STATUS_KEY, "processing");
			return response;

		} else {
			processor.scanDocuments(projectDir, path);
			return processor.getReport();
		}
	}

	/**
	 * Retrieves the result of a previously started guidance tag file processing by
	 * its process identifier.
	 * <p>
	 * This method reconstructs the path to the temporary file where the result was
	 * stored, using the provided process identifier and the system's temporary
	 * directory. If the result file exists, it reads and returns the result. If the
	 * file does not exist, it returns a status indicating that the result is still
	 * processing or unavailable.
	 * </p>
	 *
	 * @param processId The process identifier returned when processing was started;
	 *                  used to identify the result file.
	 * @return A map containing the provided {@code process_id}, a {@code status},
	 *         and either the completed {@code result} or a {@code message} when the
	 *         result is not yet available.
	 * @throws IOException If there is an error reading the result from the temp
	 *                     file.
	 */
	@Tool(name = "get_guidance_tagged_files_process_result", description = "Retrieves the result of a previously started guidance tag file processing by GUID.")
	public Object getProcessGuidanceTagFilesResult(
			@Param(name = "process_id", description = "The GUID returned when the processing was started.") String processId)
			throws IOException {

		String tempDir = ProjectLayout.getTempDir();
		File tempFile = new File(new File(tempDir, GUIDANCE_FOLDER), processId + ".tmp");

		if (!tempFile.exists()) {
			Map<String, Object> response = new HashMap<>();
			response.put(PROCESS_ID_KEY, processId);
			response.put(STATUS_KEY, "processing");
			response.put("message", "Result is not ready yet or file does not exist.");
			return response;
		}

		Object result;
		// Sonar java:S2093: close the input stream even when deserialization fails.
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile))) {
			result = ois.readObject();
		} catch (Exception e) {
			throw new IOException("Error reading guidance tag files result from temp file", e);
		}

		Map<String, Object> response = new HashMap<>();
		response.put(PROCESS_ID_KEY, processId);
		response.put(STATUS_KEY, "done");
		response.put("result", result);
		return response;
	}

	/**
	 * Provides the prompt template used to process files containing guidance tags.
	 * <p>
	 * The returned template is resolved from the {@code mcp-prompts} resource
	 * bundle and is intended for use by the guidance-tag processing workflow.
	 * </p>
	 *
	 * @param projectDir The root folder of the project, or the parent folder
	 *                   containing projects to scan. The value is accepted for the
	 *                   prompt contract and is resolved by its caller.
	 * @param path       The scanning path or pattern used to select files. The
	 *                   value is accepted for the prompt contract and is resolved
	 *                   by its caller.
	 * @return The prompt template for processing files with guidance tags.
	 */
	@Prompt(name = "process-guidance-tags", description = "Processes files with guidance tags using the configured model.")
	public String getGuidancePrompt(
			@Param(name = "path", description = "Scanning path or pattern.", defaultValue = Param.NULL) String path,
			File projectDir) {
		return mcpPromptBundle.getString("process_guidance");
	}
}
