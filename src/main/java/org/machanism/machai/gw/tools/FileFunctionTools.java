package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.macha.core.commons.configurator.Substitutor;
import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.Tool;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Installs file-system tools into a {@link Genai}.
 *
 * <p>
 * Tools in this installer are intended for host-integrated use where the host
 * controls the base working directory. All paths provided to these tools are
 * interpreted relative to the working directory supplied by the
 * provider/runtime.
 * </p>
 *
 * <h2>Installed tools</h2>
 * <ul>
 * <li>{@code read_file} – reads a file as text</li>
 * <li>{@code write_file} – writes a file (creating parent directories as
 * needed)</li>
 * <li>{@code list_files_in_directory} – lists immediate children of a
 * directory</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
public class FileFunctionTools implements FunctionTools {

	/**
	 * Default character set used when reading or writing text files.
	 */
	private static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * Lists the contents of a specified directory within a project, grouping the
	 * results into separate lists for directories and files using their relative
	 * paths.
	 *
	 * <p>
	 * This method resolves the target directory using {@code dirPath} and
	 * {@code projectDir}, verifies that it is a valid directory, and iterates
	 * through its direct children. Each child is classified as either a directory
	 * or a file and its relative path is added to the corresponding list in the
	 * returned map.
	 * </p>
	 *
	 * @param dirPath    the path to the target directory to list contents of.
	 *                   Defaults to {@code "."} (current directory).
	 * @param projectDir the root project directory used to compute relative paths
	 *                   for the listed files and folders.
	 * @return a {@link Map} containing two key-value pairs:
	 *         <ul>
	 *         <li>{@code "directories"} - a {@link List} of relative paths for all
	 *         subdirectories found.</li>
	 *         <li>{@code "files"} - a {@link List} of relative paths for all files
	 *         found.</li>
	 *         </ul>
	 *         If the specified path is not a directory or is empty, the respective
	 *         lists will be empty.
	 * @throws IOException
	 * @throws IllegalArgumentException if either path is {@code null}, cannot be
	 *                                  canonicalized, or the requested path is
	 *                                  outside {@code projectDir}
	 */
	@Tool(name = "list_files_in_directory", description = "List files and directories in a specified folder recursively, grouped by type.")
	public Map<String, List<String>> listFiles(
			@Param(name = "path", description = "The path to the directory to list contents of.", defaultValue = ".") File dirPath,
			File projectDir) throws IOException {

		File directory = getFile(dirPath, projectDir);

		List<String> directories = new ArrayList<>();
		List<String> files = new ArrayList<>();

		if (directory.exists() && directory.isDirectory()) {
			collectRecursive(directory, projectDir, directories, files);
		}

		Map<String, List<String>> result = new HashMap<>();
		result.put("directories", directories);
		result.put("files", files);

		return result;
	}

	private void collectRecursive(File currentDir, File projectDir, List<String> directories, List<String> files) {
		File[] listFiles = currentDir.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				String relativePath = getRelativePath(projectDir, file, true);
				if (file.isDirectory()) {
					directories.add(relativePath);
					collectRecursive(file, projectDir, directories, files);
				} else if (file.isFile()) {
					files.add(relativePath);
				}
			}
		}
	}

	/**
	 * Lists files recursively in a directory up to a specified maximum limit.
	 *
	 * <p>
	 * This AI functional tool returns the files discovered below a directory.
	 * </p>
	 *
	 * @param path       the relative or absolute path of the directory to scan
	 * @param maxCount   the maximum number of files allowed in the result; throws
	 *                   an error if exceeded
	 * @param projectDir the root project directory context
	 * @return a {@link List} of relative file path strings, or a message string
	 *         indicating no files were found
	 * @throws IOException
	 * @throws IllegalArgumentException if the number of discovered files exceeds
	 *                                  {@code maxCount}, or if the requested path
	 *                                  is invalid or outside {@code projectDir}
	 */
	@Tool(name = "get_recursive_file_list", description = "List files recursively in a directory (includes files in subdirectories).")
	public Object getRecursiveFiles(
			@Param(name = "path", description = "Path to the folder to list contents recursively.", defaultValue = "") File path,
			@Param(name = "max_count", description = "The maximum number of files allowed in the results. Used to prevent overly large context payloads.", defaultValue = "50") int maxCount,
			File projectDir) throws IOException {

		File targetDir = getFile(path, projectDir);

		if (targetDir == null || !targetDir.exists()) {
			return "No files found in directory.";
		}

		if (!targetDir.isDirectory()) {
			throw new IllegalArgumentException("The specified path is not a directory: " + path);
		}

		List<String> filePaths = new ArrayList<>();
		collectFilesRecursive(targetDir, projectDir, filePaths, maxCount);

		if (filePaths.isEmpty()) {
			return "No files found in the specified directory.";
		}

		return filePaths;
	}

	private void collectFilesRecursive(File currentDir, File projectDir, List<String> filePaths, int maxCount) {
		File[] listFiles = currentDir.listFiles();
		if (listFiles != null) {
			// Sort or process deterministically if needed, e.g., alphabetical order
			for (File file : listFiles) {
				if (file.isDirectory()) {
					collectFilesRecursive(file, projectDir, filePaths, maxCount);
				} else if (file.isFile()) {
					if (filePaths.size() >= maxCount) {
						throw new IllegalArgumentException(
								"Discovered file count exceeds the allowed limit of " + maxCount);
					}
					String relativePath = getRelativePath(projectDir, file, true);
					filePaths.add(relativePath);
				}
			}
		}
	}

	/**
	 * Implements {@code get_recursive_folder_list}.
	 *
	 * <p>
	 * This AI functional tool recursively discovers and returns only the folder
	 * structure (directories) within a specified path. It does not return files or
	 * contents stored inside those folders.
	 * </p>
	 *
	 * @param dir        directory path relative to {@code projectDir} to start
	 *                   scanning from
	 * @param maxCount   maximum number of folders allowed in the result
	 * @param projectDir project root used to resolve the directory
	 * @return project-relative folder paths as a list, or a message when none are
	 *         found
	 * @throws IOException
	 * @throws IllegalArgumentException if the number of discovered folders exceeds
	 *                                  {@code maxCount}, or if the requested path
	 *                                  is invalid or outside {@code projectDir}
	 */
	@Tool(name = "get_recursive_folder_list", description = "Recursively lists only the folder structure (directories) within a directory. Does not include files.")
	public Object getRecursiveFolders(
			@Param(name = "dir", description = "Path to the root folder to recursively list sub-directories for. Returns directories only, no files.", defaultValue = "") File dir,
			@Param(name = "max_count", description = "The maximum number of folders allowed in the results. Used to prevent overly large context payloads.", defaultValue = "50") int maxCount,
			File projectDir)
			throws IOException {

		File directory = getFile(dir, projectDir);

		if (!directory.exists() || !directory.isDirectory()) {
			return "No folders found in directory.";
		}

		List<String> folderPaths = new ArrayList<>();

		List<Path> paths = Files.walk(directory.toPath())
				.filter(Files::isDirectory)
				.filter(p -> !p.equals(directory.toPath()))
				.collect(Collectors.toList());

		if (paths.size() > maxCount) {
			throw new IllegalArgumentException(
					String.format(
							"Result is too long. The number of discovered folders (%d) exceeds the allowed limit of %d.",
							paths.size(), maxCount));
		}

		for (Path path : paths) {
			folderPaths.add(getRelativePath(projectDir, path.toFile(), true));
		}

		return folderPaths;
	}

	/**
	 * Implements {@code write_file}.
	 *
	 * <p>
	 * This AI functional tool creates or replaces a file with the supplied text.
	 * </p>
	 *
	 * @param filePath    file to create or replace, relative to {@code projectDir}
	 * @param text        content to write
	 * @param charsetName character set used to encode the content
	 * @param projectDir  project root used to resolve the file
	 * @return a success message or an error message when writing fails
	 * @throws IOException
	 * @throws IllegalArgumentException if the requested path is invalid or outside
	 *                                  {@code projectDir}
	 */
	@Tool(name = "write_file", description = "Write changes to a file on the file system, either by replacing content at specific positions or writing the full content.")
	public String writeFile(
			@Param(name = "path", description = "The path to the file you want to write to or create.") File filePath,
			@Param(name = "text", description = "The content to be written into the file or used as replacement.") String text,
			@Param(name = "charset", description = "The name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			File projectDir) throws IOException {
		File file = getFile(filePath, projectDir);
		if (file.exists()) {
			writeFileContent(file, text, charsetName);
			return "File updated successfully: " + filePath;
		}

		return writeNewFile(file, text, charsetName, filePath);
	}

	/**
	 * Writes {@code content} to {@code file} using {@code charsetName}.
	 *
	 * @param file        destination file
	 * @param content     content to write
	 * @param charsetName character set name
	 * @throws IOException              if writing fails
	 * @throws IllegalArgumentException if {@code charsetName} does not identify a
	 *                                  supported character set
	 */
	private void writeFileContent(File file, String content, String charsetName) throws IOException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetName))) {
			writer.write(content);
		}
	}

	/**
	 * Creates the file (and parent directories as needed) and writes {@code text}
	 * using the requested character set.
	 *
	 * @param file        file to create
	 * @param text        content
	 * @param charsetName character set name
	 * @param filePath    original (relative) file path used for messaging
	 * @return success message
	 * @throws IOException              if an I/O error occurs
	 * @throws IllegalArgumentException if {@code charsetName} does not identify a
	 *                                  supported character set
	 */
	private String writeNewFile(File file, String text, String charsetName, File filePath) throws IOException {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		writeFileContent(file, text, charsetName);
		return "File written successfully: " + filePath;
	}

	/**
	 * Implements {@code read_file}.
	 *
	 * <p>
	 * This AI functional tool reads a file and returns its text content.
	 * </p>
	 *
	 * @param filePath     file to read, relative to {@code projectDir}
	 * @param charsetName  character set used to decode the file
	 * @param projectDir   project root used to resolve the file
	 * @param configurator configuration used to substitute URL and header values
	 * @return the complete file contents as text
	 * @throws IOException              if the path is not a regular file or cannot
	 *                                  be read
	 * @throws IllegalArgumentException if the requested path is invalid or outside
	 *                                  {@code projectDir}, or {@code charsetName}
	 *                                  does not identify a supported character set
	 */
	@Tool(name = "read_file", description = "Read the contents of a file from the disk.")
	public String readFile(@Param(name = "path", description = "The path to the file to be read.") File filePath,
			@Param(name = "charset", description = "the name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			File projectDir, Configurator configurator)
			throws IOException {
		String result;

		if (configurator != null) {
			filePath = new File(Substitutor.replace(filePath.getPath(), configurator));
		}

		filePath = getFile(filePath, projectDir);
		if (!filePath.isFile()) {
			String detail = filePath.isDirectory() ? "is a directory" : "does not exist";
			throw new IOException(String.format("Expected a file, but '%s' %s.", filePath, detail));
		}
		try (FileInputStream io = new FileInputStream(filePath)) {
			result = IOUtils.toString(io, charsetName);
		}
		return result;
	}

	/**
	 * Resolves a requested path beneath the canonical project root.
	 *
	 * @param filePath   requested file or directory
	 * @param projectDir project root
	 * @return canonical file located under the project root
	 * @throws IOException
	 * @throws IllegalArgumentException if a path is invalid or escapes the root
	 */
	File getFile(File filePath, File projectDir) throws IOException {
		if (filePath == null || projectDir == null) {
			throw new IllegalArgumentException("File path and project directory must not be null.");
		}

		File baseDir = projectDir.getCanonicalFile();
		File candidate = filePath.isAbsolute() ? filePath : new File(baseDir, filePath.getPath());
		File canonicalCandidate = candidate.getCanonicalFile();
		Path basePath = baseDir.toPath();
		Path candidatePath = canonicalCandidate.toPath();
		if (!candidatePath.startsWith(basePath)) {
			throw new IllegalArgumentException("Access denied: file path is outside the project root.");
		}
		return canonicalCandidate;
	}

	/**
	 * Computes a project-relative path string.
	 *
	 * <p>
	 * The returned path always uses forward slashes ({@code /}) for consistency
	 * across platforms.
	 * </p>
	 *
	 * @param dir          base directory used to relativize the {@code file}
	 * @param file         target file or directory
	 * @param addSingleDot whether to prefix relative path with {@code ./}
	 * @return relative path, {@code .} if {@code dir} equals {@code file}, or
	 *         {@code null} when either argument is {@code null} or the paths cannot
	 *         be relativized (for example, because they use different roots)
	 */
	public static String getRelativePath(File dir, File file, boolean addSingleDot) {
		if (dir == null || file == null) {
			return null;
		}

		Path dirPath = dir.toPath().toAbsolutePath().normalize();
		Path filePath = file.toPath().toAbsolutePath().normalize();

		if (dirPath.equals(filePath)) {
			return ".";
		}

		String relativePath;
		try {
			relativePath = dirPath.relativize(filePath).toString().replace("\\", "/");
		} catch (IllegalArgumentException e) {
			return null;
		}

		if (addSingleDot && !relativePath.startsWith(".")) {
			relativePath = "./" + relativePath;
		}

		if (relativePath.isEmpty()) {
			return ".";
		}

		return relativePath;
	}

	/**
	 * Implements {@code apply_patch_to_file}.
	 *
	 * <p>
	 * This AI functional tool applies a targeted unified or simplified
	 * search-and-replace patch to a file within the project directory.
	 * </p>
	 *
	 * @param file        path of the file to patch, relative to {@code projectDir}
	 * @param patch       patch content in a supported format
	 * @param charsetName character set used to read and write the file
	 * @param projectDir  project root used to resolve the file
	 * @return a success message, or a failure message containing the underlying
	 *         error detail
	 */
	@Tool(name = "apply_patch_to_file", description = "Use this tool to update a part of a file efficiently "
			+ "by applying a targeted diff patch. Supports two formats:\n"
			+ "1. Standard Unified Diff (as produced by `diff -u` or `git diff`) containing @@ coordinates (e.g., '@@ -12,4 +12,18 @@').\n"
			+ "2. Simplified Search-and-Replace Diff containing a plain '@@' header with exact line-matching blocks "
			+ "starting with '-' (lines to find and remove) and '+' (lines to insert).\n"
			+ "Make sure your patch matches the surrounding target context uniquely to ensure successful application.")
	public String applyPatchToFile(
			@Param(name = "file", description = "The path to the file to be patched.") File file,
			@Param(name = "patch", description = "The unified diff patch to apply.") String patch,
			@Param(name = "charset", description = "The name of the requested charset.", defaultValue = DEFAULT_CHARSET) String charsetName,
			File projectDir) {
		try {
			List<String> patchLines = Arrays.asList(patch.split("\\r?\\n"));
			file = getFile(file, projectDir);
			PatchApplier.applyPatch(file, patchLines, Charset.forName(charsetName));
			return "Patch applied successfully.";
		} catch (Exception e) {
			return "Failed to apply patch: " + e.getMessage();
		}
	}
}
