package org.machanism.machai.gw.reviewer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.machanism.machai.gw.processor.GuidanceProcessor;
import org.machanism.machai.project.layout.ProjectLayout;

/**
 * {@link Reviewer} implementation for Python source files ({@code .py}).
 *
 * <p>Guidance may appear in either single-line comments ({@code #}) or in triple-quoted strings that contain
 * the {@link GuidanceProcessor#GUIDANCE_TAG_NAME @guidance} tag.
 */
public class BasicReviewer implements Reviewer {

	/** Resource bundle containing prompt templates for reviewed files. */
	private final ResourceBundle promptBundle = ResourceBundle.getBundle("document-prompts");

	/**
	 * Matches a VBA line comment carrying the guidance tag:
	 *   ' GUIDANCE: text
	 *   Rem GUIDANCE: text
	 * Also matches trailing comments after code (e.g. {@code x = 1 ' GUIDANCE: text}),
	 * but only when the apostrophe is not inside a string literal.
	 */
	private static final Pattern VBA_GUIDANCE_PATTERN = Pattern.compile(
			"(?im)^(?:[^\"'\\r\\n]|\"[^\"\\r\\n]*\")*?(?:'|\\bRem\\b)[ \\t]*"
					+ Pattern.quote(GuidanceProcessor.GUIDANCE_TAG_NAME) + "[ \\t]*:?[ \\t]*(.*)$");

	/** @return an array containing {@code "bas"}, {@code "cls"} and {@code "frm"} */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "bas", "cls", "frm" };
	}

	/**
	 * Reviews a VBA file for line-comment guidance.
	 *
	 * @param projectDir the project root used to compute a relative path
	 * @param guidancesFile the VBA file to inspect
	 * @return a formatted prompt, or {@code null} when no non-blank guidance is found
	 * @throws IOException if the file cannot be read
	 */
	@Override
	public String perform(File projectDir, File guidancesFile) throws IOException {
		String content = new String(Files.readAllBytes(guidancesFile.toPath()), StandardCharsets.UTF_8);
		if (!content.contains(GuidanceProcessor.GUIDANCE_TAG_NAME)) {
			return null;
		}

		Matcher lineMatcher = VBA_GUIDANCE_PATTERN.matcher(content);
		if (lineMatcher.find()) {
			String guidanceText = lineMatcher.group(1);
			if (StringUtils.isBlank(guidanceText)) {
				return null;
			}
			return MessageFormat.format(promptBundle.getString("basic_file"), guidancesFile.getName(),
					ProjectLayout.getRelativePath(projectDir, guidancesFile), guidanceText.trim());
		}

		return null;
	}

}
