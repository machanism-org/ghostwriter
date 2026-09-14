package org.machanism.machai.gw.processor;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Signals that an episode requested by name could not be resolved.
 *
 * <p>The unresolved name is retained as this exception's detail message so callers can
 * report or diagnose the failed lookup.</p>
 */
public class EpisodeNotFoundException extends RuntimeException {

	/**
	 * Serialization version identifier for this exception type.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception for an episode name that could not be resolved.
	 *
	 * <p>The supplied name becomes this exception's detail message.</p>
	 *
	 * @param episodeName the unresolved episode name; may be {@code null} when no name was supplied
	 */
	public EpisodeNotFoundException(String episodeName) {
		super(episodeName);
	}

}
