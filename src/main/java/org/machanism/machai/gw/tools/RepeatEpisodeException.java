package org.machanism.machai.gw.tools;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Runtime control-flow exception that requests repetition of the current Act
 * episode.
 *
 * <p>Processors catch this intentional signal and restart the current episode
 * while preserving the applicable workflow context. This exception represents
 * an expected workflow decision rather than a processing failure.</p>
 *
 * @author Viktor Tovstyi
 */
public class RepeatEpisodeException extends RuntimeException {
	/**
	 * Serialization version for this control-flow exception.
	 *
	 * @serial
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an exception that requests repetition of the current Act episode.
	 *
	 * <p>The exception message identifies the request when it is recorded by
	 * workflow diagnostics.</p>
	 */
	public RepeatEpisodeException() {
		super("Repeat current episode requested.");
	}
}
