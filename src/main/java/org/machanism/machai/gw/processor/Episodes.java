package org.machanism.machai.gw.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.machanism.machai.gw.tools.MoveToEpisodeException;
import org.machanism.machai.gw.tools.RepeatEpisodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*@guidance: >>> ${guidances}/def-class-javadoc.md 
 * 
 * Class javadoc should describe supported functionality and provide examples to use it.
 * If the method used as Javadoc documentation is not public or protected, the method name should not be specified.
 */
/**
 * Maintains an ordered collection of act episode prompts and provides execution
 * helpers that support several playback strategies.
 *
 * <p>
 * Supported functionality:
 * </p>
 * <ul>
 * <li>Holds an ordered list of episode prompts, each optionally starting with a
 * markdown heading ({@code "# Name"}) that names the episode.</li>
 * <li>Executes episodes in their natural order via
 * {@link #regularOrder(Integer, BiFunction)}, honoring repeat requests
 * ({@link RepeatEpisodeException}) and jump/redirect requests
 * ({@link MoveToEpisodeException}).</li>
 * <li>Executes an explicitly selected subset of episodes in the requested order
 * via {@link #requestedOrder(BiFunction)}.</li>
 * <li>Resolves episode indices either by 1-based ID or by heading name when a
 * {@link MoveToEpisodeException} requests a jump to a named episode.</li>
 * <li>Exposes act/episode metadata for reporting via
 * {@link #getActInformation(int)}.</li>
 * </ul>
 *
 * <p>
 * <b>Example: regular order execution</b>
 * </p>
 *
 * <pre>
 * Episodes episodes = new Episodes(actProcessor);
 * episodes.setName("demo-act");
 * episodes.setEpisodes(List.of(
 * 		"# Introduction\nWelcome to the show!",
 * 		"# Recap\nLast time on our show..."));
 *
 * episodes.regularOrder(1, (id, prompt) -&gt; executor.run(id, prompt));
 * </pre>
 *
 * <p>
 * <b>Example: executing only a selected subset</b>
 * </p>
 *
 * <pre>
 * episodes.setSelectedEpisodes(List.of(2));
 * if (!episodes.isRegularOrder()) {
 * 	episodes.requestedOrder((id, prompt) -&gt; executor.run(id, prompt));
 * }
 * </pre>
 *
 */
public class Episodes {
    /** Prefix that identifies a first-level Markdown episode heading. */
    private static final String HEADER_MARKER = "# ";

    /** Logger for documentation input processing events. */
    private static final Logger logger = LoggerFactory.getLogger(Episodes.class);

    /** Ordered list of act episode prompts to execute. */
    private List<String> episodePrompts = new ArrayList<>();

    /** Explicitly selected 1-based episode identifiers. */
    private List<Integer> selectedEpisodes = new ArrayList<>();

    /** Logical act name associated with the episodes. */
    private String name;

    /** Processor that receives the result produced by each completed episode. */
    private ActProcessor actProcessor;

    /**
     * Creates an episode collection whose execution results are recorded by the
     * supplied processor.
     *
     * @param actProcessor processor that receives completed episode results
     */
    public Episodes(ActProcessor actProcessor) {
        this.actProcessor = actProcessor;
    }

    /**
     * Sets the list of explicitly requested episode identifiers.
     *
     * @param selectedEpisodeIds 1-based episode identifiers to execute
     * @throws IllegalArgumentException if any identifier is outside the available
     *                                  episode range
     */
    public void setSelectedEpisodes(List<Integer> selectedEpisodeIds) {
        int numberOfEpisodes = episodePrompts.size();
        boolean hasInvalidId = selectedEpisodeIds.stream().anyMatch(id -> id <= 0 || id > numberOfEpisodes);
        if (hasInvalidId) {
            throw new IllegalArgumentException(
                    "All episode IDs must be between 1 and " + numberOfEpisodes + "  (inclusive).");
        }
        this.selectedEpisodes = selectedEpisodeIds;
    }

    /**
     * Finds the 1-based identifier of the episode with the supplied heading.
     *
     * @param episodeName heading name to locate
     * @return the matching 1-based episode identifier
     * @throws EpisodeNotFoundException if no episode has the requested heading
     */
    private int getEpisodeIdByName(String episodeName) {
        for (int id = 1; id <= episodePrompts.size(); id++) {
            String firstHeaderLine = getEpisodeName(id);
            if (episodeName.equals(firstHeaderLine)) {
                return id;
            }
        }
        throw new EpisodeNotFoundException(episodeName);
    }

    /**
     * Extracts an episode's first-level Markdown heading, excluding optional
     * YAML-style front matter.
     *
     * @param episodeId 1-based identifier of the episode to inspect
     * @return the normalized heading text, or {@code null} when no heading exists
     * @throws IndexOutOfBoundsException if the identifier does not address an episode
     */
    private String getEpisodeName(int episodeId) {
        String episode = StringUtils.trim(episodePrompts.get(episodeId - 1));
        if (Strings.CS.startsWith(episode, "---")) {
            episode = StringUtils.substringAfter(StringUtils.substring(episode, 3), "---").trim();
        }
        String header = episode != null && episode.startsWith(HEADER_MARKER)
                ? StringUtils.substringBetween(episode, HEADER_MARKER, "\n")
                : null;
        return StringUtils.trimToNull(header);
    }

    /**
     * Executes episodes in regular order starting from the supplied 1-based index
     * while honoring repeat and move requests.
     *
     * @param startEpisodeId starting 1-based episode index
     * @param func callback used to execute an episode
     * @throws IndexOutOfBoundsException if a requested episode index is invalid
     */
    public void regularOrder(Integer startEpisodeId, BiFunction<Integer, String, String> func) {
        Integer moveToEpisodeId = startEpisodeId;
        while (moveToEpisodeId != null) {
            moveToEpisodeId = executeRegularEpisodes(moveToEpisodeId, func);
        }
    }

    /**
     * Executes consecutive episodes until completion or a move request changes
     * the next episode to execute.
     *
     * @param startEpisodeId 1-based identifier at which execution begins
     * @param func callback used to execute each episode
     * @return the requested destination after a move, or {@code null} on completion
     * @throws IndexOutOfBoundsException if an episode identifier is invalid
     * @throws EpisodeNotFoundException if a named move destination does not exist
     */
    private Integer executeRegularEpisodes(int startEpisodeId, BiFunction<Integer, String, String> func) {
        try {
            for (int episodeId = startEpisodeId; episodeId <= episodePrompts.size(); episodeId++) {
                executeEpisodeWithRepeats(episodeId, func);
            }
            return null;
        } catch (MoveToEpisodeException exception) {
            return getEpisodeId(null, exception);
        }
    }

    /**
     * Executes only the explicitly selected episodes in their requested order.
     *
     * @param func callback used to execute an episode
     * @return the last processed episode identifier, or {@code 0} when none are selected
     * @throws IndexOutOfBoundsException if a selected episode identifier is invalid
     */
    public int requestedOrder(BiFunction<Integer, String, String> func) {
        int episodeId = 0;
        for (Integer selectedEpisodeId : selectedEpisodes) {
            episodeId = selectedEpisodeId;
            executeEpisodeWithRepeats(episodeId, func);
        }
        return episodeId;
    }

    /**
     * Executes an episode repeatedly until its callback completes without asking
     * for another iteration.
     *
     * @param episodeId 1-based identifier of the episode to execute
     * @param func callback used to execute the episode
     * @throws IndexOutOfBoundsException if the identifier does not address an episode
     */
    private void executeEpisodeWithRepeats(int episodeId, BiFunction<Integer, String, String> func) {
        int iteration = 1;
        boolean repeat;
        do {
            repeat = !executeEpisode(episodeId, iteration++, func);
        } while (repeat);
    }

    /**
     * Runs one iteration of an episode and records its result when completed.
     *
     * @param episodeId 1-based identifier of the episode to execute
     * @param iteration current execution iteration, starting at {@code 1}
     * @param func callback used to execute the episode
     * @return {@code true} when the iteration completed, or {@code false} when it
     *         requested a repeat
     * @throws IndexOutOfBoundsException if the identifier does not address an episode
     */
    private boolean executeEpisode(int episodeId, int iteration, BiFunction<Integer, String, String> func) {
        try {
            String episode = episodePrompts.get(episodeId - 1);
            logEpisodeHeader(episodeId, iteration, "Start");
            String perform = func.apply(episodeId, episode);
            logEpisodeHeader(episodeId, iteration, "End");
            actProcessor.addResults(perform);
            logResult(perform);
            return true;
        } catch (RepeatEpisodeException exception) {
            return false;
        }
    }

    /**
     * Logs a nonblank execution result using the standard output prefix.
     *
     * @param perform result returned by an episode callback
     */
    private void logResult(String perform) {
        if (StringUtils.isNoneBlank(perform)) {
            logger.info(AIFileProcessor.LOG_OUTPUT_PREFIX, perform);
        }
    }

    /**
     * Resolves the next episode index from a move request exception.
     *
     * @param requestedEpisodeId current fallback episode index
     * @param exception exception describing the requested move
     * @return resolved 1-based episode index
     * @throws EpisodeNotFoundException if the requested episode name does not exist
     */
    public Integer getEpisodeId(Integer requestedEpisodeId, MoveToEpisodeException exception) {
        Integer episodeId = exception.getEpisodeId();
        if (episodeId != null) {
            return episodeId;
        }
        if (exception.getName() != null) {
            return getEpisodeIdByName(exception.getName());
        }
        return requestedEpisodeId;
    }

    /**
     * Logs a visual boundary around an episode execution when episode or iteration
     * information is useful.
     *
     * @param episodeId 1-based identifier of the episode being logged
     * @param iteration current execution iteration
     * @param msg boundary label, such as {@code Start} or {@code End}
     * @throws IndexOutOfBoundsException if the identifier does not address an episode
     */
    private void logEpisodeHeader(int episodeId, int iteration, String msg) {
        if ((episodePrompts.size() > 1 || iteration > 1) && logger.isInfoEnabled()) {
            String iterationLabel = iteration > 1 ? " [Iteration: " + iteration + "]) " : " ";
            String episodeName = getEpisodeName(episodeId);
            String displayName = episodeName == null ? StringUtils.EMPTY : " \"" + episodeName + "\"";
            String title = " " + msg + " Episode #" + episodeId + displayName + iterationLabel + " ";
            logger.info("{}", StringUtils.center(title, GWConstants.LOG_LINE_LENGTH, "-"));
        }
    }

    /**
     * Replaces the ordered prompts available for execution.
     *
     * @param episodes ordered list of episode prompts
     */
    public void setEpisodes(List<String> episodes) {
        this.episodePrompts = episodes;
    }

    /**
     * Returns the ordered episode prompts.
     *
     * @return ordered list of episode prompts
     */
    public List<String> getEpisodes() {
        return episodePrompts;
    }

    /**
     * Determines whether all episodes should execute in their natural order.
     *
     * @return {@code true} if no explicit episode selection exists; otherwise
     *         {@code false}
     */
    public boolean isRegularOrder() {
        return selectedEpisodes.isEmpty();
    }

    /**
     * Returns the number of configured episode prompts.
     *
     * @return number of configured episodes
     */
    public int size() {
        return episodePrompts.size();
    }

    /**
     * Builds metadata describing every configured episode and the current episode.
     *
     * @param episodeId 1-based identifier of the current episode
     * @return map containing episode metadata and the current episode identifier
     */
    public Map<String, Object> getActInformation(int episodeId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> episodesArray = new ArrayList<>();
        for (int id = 1; id <= episodePrompts.size(); id++) {
            Map<String, String> episodeObj = new HashMap<>();
            episodeObj.put("ID", Objects.toString(id));
            episodeObj.put("EPISODE_NAME", getEpisodeName(id));
            episodesArray.add(episodeObj);
        }
        result.put("EPISODES", episodesArray);
        result.put("CURRENT_EPISODE_ID", episodeId);
        result.put("ACT_INFORMATION", episodesArray);
        return result;
    }

    /**
     * Returns the logical name associated with this act.
     *
     * @return act name, or {@code null} when no name has been assigned
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the logical name associated with this act.
     *
     * @param name act name to assign
     */
    public void setName(String name) {
        this.name = name;
    }
}
