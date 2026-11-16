package org.machanism.machai.gw.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.process.tools.CommandResult;

class CommandFunctionToolsExecutionTest {

	@TempDir
	Path projectDirectory;

	@Test
	void executeCommandRejectsInvalidWorkingDirectoryBeforeStartingProcess() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();

		// Act
		CommandResult result = tools.executeCommand("echo ignored", null, "..", 128, "UTF-8",
				projectDirectory.toFile(), null);

		// Assert
		assertEquals("Error: Invalid working directory.", result.getMessage());
	}

	@Test
	void executeCommandCapturesOutputAndReturnsSuccessfulReport() throws Exception {
		// Arrange
		CommandFunctionTools tools = new CommandFunctionTools();
		Configurator configurator = permissiveConfigurator();
		String command = System.getProperty("os.name").toLowerCase().contains("win")
				? "cmd /c echo command-coverage"
				: "/usr/bin/printf command-coverage";

		// Act
		Object result = tools.executeCommand(command, Collections.emptyMap(), ".", 128, "UTF-8",
				projectDirectory.toFile(), configurator);

		// Assert
		CommandResult report = assertInstanceOf(CommandResult.class, result);
		assertEquals(0, report.getExitCode());
		Map<?, ?> log = assertInstanceOf(Map.class, report.getLogReport());
		assertTrue(log.toString().contains("command-coverage"));
	}

	private Configurator permissiveConfigurator() {
		Configurator configurator = mock(Configurator.class);
		when(configurator.get("ft.command.denylist", null)).thenReturn("");
		return configurator;
	}
}
