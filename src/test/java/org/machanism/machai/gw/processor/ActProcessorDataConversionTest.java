package org.machanism.machai.gw.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tomlj.Toml;

/** Verifies TOML scalar conversion without relying on a provider workflow. */
class ActProcessorDataConversionTest {

    @Test
    void setActData_convertsLongAndNestedTomlValuesAndIgnoresUnsupportedTypes() {
        // Arrange
        Map<String, Object> properties = new HashMap<>();

        // Act
        ActProcessor.setActData(properties, Toml.parse("large = 2147483648\n[section]\nname = \"value\"\n"));

        // Assert
        assertNull(properties.get("large"));
        assertEquals("value", properties.get("section.name"));
    }
}
