package org.machanism.machai.gw.tools;

import java.io.File;

import org.machanism.machai.ai.provider.Genai;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Param;
import org.machanism.machai.ai.tools.SupportedFor;
import org.machanism.machai.ai.tools.Tool;
import org.machanism.machai.gw.processor.AIFileProcessor;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Provides function tools for task and process-execution control within an
 * {@link AIFileProcessor} workflow.
 *
 * <p>The tool infrastructure exposes the annotated methods to the AI provider.
 * Invoking a tool signals task completion or application termination by throwing
 * the corresponding control-flow exception; the workflow host is responsible for
 * handling that signal.</p>
 *
 * @author Viktor Tovstyi
 */
@SupportedFor({ AIFileProcessor.class })
public class CommandSpecFunctionTools implements FunctionTools {

	/**
	 * Default explanatory message used when a control-flow tool is invoked without
	 * an explicit message.
	 */
	private static final String TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE = "Execution terminated by function tool.";

	/**
	 * Requests application termination by throwing a {@link ProcessTerminationException}.
	 * The exception carries the supplied message and exit code for handling by the
	 * workflow host. When omitted by the caller, the tool infrastructure supplies the
	 * annotation-defined defaults for {@code message} and {@code exitCode}.
	 *
	 * <p>The {@code projectDir} argument is supplied by the tool infrastructure as
	 * invocation context and is not used when constructing the termination request.</p>
	 *
	 * @param message human-readable message exposed to the host
	 * @param exitCode process exit code requested from the host
	 * @param projectDir project directory provided as invocation context; unused
	 * @return never returns normally because it always requests termination
	 * @throws ProcessTerminationException always, to request process termination
	 */
	@Tool(name = "terminate-execution", description = "Terminates the application by sending an exit code. This function tool should only be used when explicitly requested by the user.  "
			+ "Do not call this function automatically if task completed successfully.")
	public String terminateExecution(
			@Param(name = "message", description = "The exception message to use.", defaultValue = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE) String message,
			@Param(name = "exit-code", description = "The exit code to return when terminating the execution. Defaults to 0 if not specified.", defaultValue = "0") int exitCode,
			@Param(name = "project-dir", description = "The project dir.") File projectDir) {
		throw new ProcessTerminationException(message, exitCode);
	}

	/**
	 * Completes the current task by throwing an {@link EndTaskException}. The host
	 * remains active and can accept subsequent tasks. When no message is supplied,
	 * the tool infrastructure uses its annotation-defined default message.
	 *
	 * @param message human-readable message describing task completion
	 * @return never returns normally because it always signals task completion
	 * @throws EndTaskException always, to request task completion
	 */
	@Tool(name = "end-task", description = "Use this function if the user has requested to `end the task`. Ends the current task without terminating the application. "
			+ "Use this function to conclude an interactive session with the user, ensuring that only the current task is finished while the application remains active. "
			+ "This tool is ideal for gracefully completing user-driven tasks in interactive mode, "
			+ "allowing further operations or tasks to continue.")
	public String endTask(
			@Param(name = "message", description = "The message to use upon completion.", defaultValue = TASK_TERMINATED_BY_FUNCTION_TOOL_MESSAGE) String message) {
		throw new EndTaskException(message);
	}

}
