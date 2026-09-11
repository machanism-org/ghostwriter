package org.machanism.machai.gw.processor;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Exception thrown when an episode cannot be resolved by name.
 */
public class EpisodeNotFoundException extends RuntimeException {

	/**
	 * Serialization version identifier for this exception type.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception that identifies the episode name that could not be resolved.
	 *
	 * @param episodeName the unresolved episode name; may be {@code null} when no name was supplied
	 */
	public EpisodeNotFoundException(String episodeName) {
		super(episodeName);
	}

}
