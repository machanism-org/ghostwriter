package org.machanism.machai.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.machanism.machai.project.layout.ProjectLayout;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * A {@link StringBuilder}-like helper that retains only the last
 * {@code maxSize} characters.
 *
 * <p>
 * This utility is typically used when capturing potentially unbounded output
 * (for example, process stdout/stderr) while keeping a deterministic upper
 * bound on memory usage. It also supports optional persistence of appended
 * content to a log file on disk.
 * </p>
 *
 * <p>
 * The log buffer is truncated from the beginning if the maximum size is
 * exceeded, and a flag is set to indicate truncation. The class also tracks the
 * total number of characters ever appended and the elapsed time since
 * instantiation.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class LogBuilder {

	/**
	 * Name of the directory beneath the runtime temporary directory where this
	 * builder stores its persisted log file.
	 * <p>
	 * This directory is combined with {@link ProjectLayout#getTempDir()} when a
	 * log path is requested.
	 * </p>
	 */
	private final String folder;

	/**
	 * Standard filename extension assigned to persisted command log files.
	 * <p>
	 * All log files created by this class use this extension after their log
	 * identifier.
	 * </p>
	 */
	public static final String LOG_EXTENSION = ".log";

	/**
	 * Maximum number of characters retained in {@link #sb} after each append.
	 */
	private final int maxSize;
	/**
	 * Mutable buffer containing the most recently appended, retained log content.
	 */
	private final StringBuilder sb;
	/**
	 * Whether content has been removed from the beginning of the buffer since the
	 * last call to {@link #clear()}.
	 */
	private boolean truncated;
	/**
	 * Optional identifier used as the base name of the persisted log file.
	 */
	private final String logId;
	/**
	 * Optional marker indicating that appended content should also be persisted.
	 * The directory itself is not used to construct the log path.
	 */
	private final File projectDir;
	/**
	 * Total number of characters accepted by {@link #append(String)} since this
	 * instance was created, including content no longer retained in {@link #sb}.
	 */
	private int totalLength;
	/**
	 * Epoch time in milliseconds at which this builder was created.
	 */
	private final long startTime;

	/**
	 * Creates a builder that keeps at most {@code maxSize} characters.
	 * 
	 * @param folder     directory beneath the runtime temporary directory for the
	 *                   persisted log file
	 * @param maxSize    maximum number of characters to retain; must be positive
	 * @param logId      optional log identifier for file persistence
	 * @param projectDir optional non-null marker enabling file persistence when
	 *                   {@code logId} is also non-null
	 *
	 * @throws IllegalArgumentException if {@code maxSize} is not positive
	 */
	public LogBuilder(String folder, int maxSize, String logId, File projectDir) {
		this.folder = folder;
		startTime = System.currentTimeMillis();
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize must be positive");
		}
		this.maxSize = maxSize;
		this.sb = new StringBuilder();
		this.truncated = false;
		this.logId = logId;
		this.projectDir = projectDir;
	}

	/**
	 * Appends the specified text to the internal log buffer and optionally persists
	 * it to disk.
	 *
	 * <p>
	 * This method updates the internal buffer by adding the provided text. If the
	 * buffer exceeds the configured maximum size ({@code maxSize}), the oldest
	 * content is truncated to maintain the limit. The {@code truncated} flag is set
	 * if truncation occurs.
	 * </p>
	 *
	 * <p>
	 * If both {@code projectDir} and {@code logId} are set, the appended text is
	 * also written to a log file on disk. The log file is created if it does not
	 * exist, or appended to if it does. Parent directories are created as needed.
	 * </p>
	 *
	 * @param text the text to append to the log buffer; if {@code null}, no action
	 *             is taken
	 * @return this {@code LogBuilder} instance for method chaining
	 * @throws java.io.UncheckedIOException if an I/O error occurs while writing to
	 *                                     the log file
	 */
	public LogBuilder append(String text) {
		if (text == null) {
			return this;
		}
		totalLength = getTotalLength() + text.length();
		sb.append(text);

		int excess = sb.length() - maxSize;
		if (excess > 0) {
			sb.delete(0, excess);
			truncated = true;
		}

		if (projectDir != null && logId != null) {
			Path logPath = getCommandLogPath(folder, logId);
			try {
				Files.createDirectories(logPath.getParent());
				Files.write(logPath, text.getBytes(StandardCharsets.UTF_8),
						Files.exists(logPath) ? java.nio.file.StandardOpenOption.APPEND
								: java.nio.file.StandardOpenOption.CREATE);
			} catch (IOException e) {
				// Sonar java:S112: retain the I/O failure category for callers.
				throw new java.io.UncheckedIOException("Unable to append command log", e);
			}
		}

		return this;
	}

	/**
	 * Returns the path to the log file for the given log identifier.
	 *
	 * <p>
	 * The log file is located in the system temporary directory under
	 * {@code gw-command-logs}. Parent directories are created if necessary.
	 * </p>
	 *
	 * @param folder the directory beneath the runtime temporary directory
	 * @param logId  the log identifier used as the file name
	 * @return the path to the log file
	 * @throws java.io.UncheckedIOException if the log directory cannot be created
	 */
	public static Path getCommandLogPath(String folder, String logId) {
		String tempDir = ProjectLayout.getTempDir();
		Path logDir = new File(tempDir, folder).toPath();
		try {
			Files.createDirectories(logDir);
		} catch (IOException e) {
			// Sonar java:S112: retain the I/O failure category for callers.
			throw new java.io.UncheckedIOException("Failed to create log directory: " + logDir, e);
		}
		return logDir.resolve(logId + LOG_EXTENSION);
	}

	/**
	 * Returns the retained content.
	 *
	 * <p>
	 * Earlier content is omitted when the configured maximum size was exceeded;
	 * callers can inspect {@link #getReport()} for the truncation status.
	 * </p>
	 *
	 * @return retained text (possibly with a truncation prefix)
	 */
	public String getTail() {
		return sb.toString();
	}

	/**
	 * Returns the number of characters currently retained.
	 *
	 * @return retained length
	 */
	public int length() {
		return sb.length();
	}

	/**
	 * Clears the retained content and resets the truncation flag.
	 *
	 * <p>
	 * This operation does not reset the total appended length, the start time, or
	 * any persisted log file.
	 * </p>
	 */
	public void clear() {
		sb.setLength(0);
		truncated = false;
	}

	/**
	 * Returns the total number of characters ever appended to this builder.
	 *
	 * @return the total length of all appended content
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * Returns a report of the log state, including log ID, retained tail, total
	 * length, truncation status, and elapsed process time in milliseconds.
	 *
	 * @return a map containing log state information
	 */
	public Map<String, Object> getReport() {
		Map<String, Object> report = new HashMap<>();
		report.put("command_log_id", logId);
		report.put("tail", sb.toString());
		report.put("total_length", totalLength);
		report.put("truncated", truncated);
		report.put("processTime_ms", System.currentTimeMillis() - startTime);

		return report;
	}

}
