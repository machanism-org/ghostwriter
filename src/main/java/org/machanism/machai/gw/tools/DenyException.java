package org.machanism.machai.gw.tools;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Signals that a command has failed a deny-list security check and must not be executed.
 *
 * <p>This exception is used by {@link CommandSecurityChecker} when the supplied command line matches a deny-list
 * rule, such as a keyword or regular expression. Host-side command execution tools can catch it and refuse to run
 * the command.</p>
 *
 * @author Viktor Tovstyi
 * @see CommandSecurityChecker
 */
public class DenyException extends Exception {

	/** Serialization version for this exception type. */
	private static final long serialVersionUID = 3508879826779125812L;

	/**
	 * Creates a new exception describing the deny-list rule that rejected the command.
	 *
	 * @param message description of the deny-list rule that matched
	 */
	public DenyException(String message) {
		super(message);
	}

}
