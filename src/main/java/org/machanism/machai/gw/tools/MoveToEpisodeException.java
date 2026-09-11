package org.machanism.machai.gw.tools;

import org.machanism.machai.ai.tools.SpecialException;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Control-flow exception that requests navigation to a named or numbered Act
 * episode.
 *
 * <p>Embedding processors interpret this exception as an intentional workflow
 * transition rather than an application error. The target may be identified by
 * its one-based numeric identifier, its name, or neither to request the next
 * episode.</p>
 *
 * @author Viktor Tovstyi
 */
public class MoveToEpisodeException extends SpecialException {
	/**
	 * Serialization version for compatibility when this control-flow exception is
	 * serialized.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Optional one-based identifier of the requested target episode.
	 *
	 * <p>A {@code null} value indicates that navigation is not targeted by a
	 * numeric identifier.</p>
	 */
	private final Integer episodeId;
	/**
	 * Optional name of the requested target episode.
	 *
	 * <p>A {@code null} value indicates that navigation is not targeted by a
	 * name.</p>
	 */
	private final String episodeName;

	/**
	 * Creates a navigation request for an episode identifier or name.
	 *
	 * <p>If {@code episodeId} is {@code null}, the exception message describes a
	 * request to move to the next episode; otherwise, it identifies the supplied
	 * numeric target. The episode name is retained independently and may be used
	 * by the receiving workflow processor.</p>
	 *
	 * @param episodeId target one-based episode identifier, or {@code null}
	 * @param name target episode name, or {@code null} when navigation is not
	 *             targeted by name
	 */
	public MoveToEpisodeException(Integer episodeId, String name) {
		super(episodeId == null ? "Move to next episode" : "Move to episode: " + episodeId);
		this.episodeId = episodeId;
		this.episodeName = name;
	}

	/**
	 * Returns the requested target episode identifier.
	 *
	 * @return the one-based episode identifier, or {@code null} when the target
	 *         was not specified by identifier
	 */
	public Integer getEpisodeId() {
		return episodeId;
	}

	/**
	 * Returns the requested target episode name.
	 *
	 * @return the target episode name, or {@code null} when the target was not
	 *         specified by name
	 */
	public String getName() {
		return episodeName;
	}
}
