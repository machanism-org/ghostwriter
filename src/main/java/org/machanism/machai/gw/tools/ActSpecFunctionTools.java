package org.machanism.machai.gw.tools;

import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;
import org.machanism.machai.gw.processor.ActProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Provides AI-callable functional tools for episode navigation and control
 * within an {@link ActProcessor} context.
 * <p>
 * This {@link FunctionTools} implementation registers tools for moving to a
 * requested episode and for repeating the current episode in a project workflow.
 * It is intended for use with {@link ActProcessor} and integrates with the
 * {@link Genai} provider.
 * </p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor({ ActProcessor.class })
public class ActSpecFunctionTools implements FunctionTools {

	/**
	 * Logger used to emit the optional response message before the current episode
	 * is repeated.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ActSpecFunctionTools.class);

	/**
	 * AI functional tool that requests navigation to the episode identified by its
	 * {@code id} or {@code name}. Use this tool only when the user explicitly
	 * requests a particular episode; the system handles sequential navigation to
	 * the next episode automatically.
	 * <p>
	 * The method does not return normally. It throws a
	 * {@link MoveToEpisodeException}, which the surrounding act-processing flow
	 * interprets as a navigation request.
	 * </p>
	 *
	 * @param targetId the identifier of the requested episode
	 * @param name the name of the requested episode
	 * @throws MoveToEpisodeException always thrown to signal episode navigation
	 */
	@Tool(name = "move-to-episode", description = "Moves to a specific episode ONLY when the user explicitly requests to navigate to an episode by its 'id' or 'name'. "
			+ "Do NOT call this tool for moving sequentially to the 'next' episode, as the system does this automatically by default.")
	public void moveToEpisode(@Param(name = "id", description = "The ID of the episode to move to.") int targetId,
			@Param(name = "name", description = "The name of the episode to move to.") String name) {
		throw new MoveToEpisodeException(targetId, name);
	}

	/**
	 * AI functional tool that repeats the current episode by terminating its
	 * execution and restarting the same episode while preserving its context.
	 * <p>
	 * This method can re-execute an episode after a validation failure or when
	 * additional user input is required. When {@code message} is non-empty, it is
	 * emitted to the configured log before repetition is requested. The method does
	 * not return normally.
	 * </p>
	 *
	 * @param message a non-null custom response message to log before repeating
	 *                the episode; if empty, no message is logged
	 * @throws RepeatEpisodeException always thrown to signal the episode should be
	 *                                repeated
	 */
	@Tool(name = "repeate-episode", description = "Repeats the current episode. This function terminates the current execution and restarts the same "
			+ "episode, preserving the context.")
	public void repeateEpisode(
			@Param(name = "message", description = "A custom response message to output before repeating the episode.", defaultValue = "") String message) {
		if (!message.isEmpty()) {
			logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, message);
		}
		throw new RepeatEpisodeException();
	}

}
