package org.machanism.machai.gw.processor;

import java.io.FileNotFoundException;

/**
 * Signals that an act definition could not be found in the configured act
 * locations or on the classpath.
 *
 * <p>This exception extends {@link FileNotFoundException} so callers can handle
 * a missing act in the same way as other missing file resources.</p>
 */
public class ActNotFoundException extends FileNotFoundException {

	/**
	 * Serialization version identifier for this exception type.
	 */
	private static final long serialVersionUID = 534465068798868999L;

	/**
	 * Creates an exception for an act that could not be found.
	 *
	 * @param name the name of the missing act; used as the exception detail message
	 */
	public ActNotFoundException(String name) {
		super(name);
	}

}
