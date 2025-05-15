package io.github.eisopux.diagnostics.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CompilationReportData serves as a central container for aggregating data collected during a
 * compilation run. The data is organized into sections, where each section is identified by a
 * unique string key and is represented as a list of key/value pairs (i.e. a {@code List<Map<String,
 * Object>>}). {@link Collector} implementations should conform the data they collect to this
 * mapping
 */
public class CompilationReportData {

    /**
     * Maps section identifiers to the data belonging to that section.
     *
     * <ul>
     *   <li><strong>Key</strong> – a human-readable string that uniquely names the section
     *       (for example {@code "diagnostics"})
     *
     *   <li><strong>Value</strong> – a list whose elements are {@code Map<String, Object>}
     *       instances.  Each inner map represents one logical record (row) of data for
     *       the section. Keys within this inner map are descriptors and the
     *       corresponding values are data points (for example, a {@code "diagnostics"} section
     *       may have key {@code "kind"} with value {@code "ERROR"} or key {@code "line"}
     *       with value {@code 42})</li>
     * </ul>
     */
    private final Map<String, List<Map<String, Object>>> sections = new HashMap<>();

    /**
     * Associates the specified section data with the given section identifier.
     *
     * @param sectionId the unique identifier for the section (e.g., "diagnostics")
     * @param sectionData a list of key/value pair mappings representing the section's data
     */
    public void putSection(String sectionId, List<Map<String, Object>> sectionData) {
        sections.put(sectionId, sectionData);
    }

    /**
     * Retrieves the section data associated with the specified section identifier.
     *
     * @param sectionId the unique identifier for the section
     * @return the list of key/value pair mappings for the section, or {@code null} if the section
     *     does not exist
     */
    public List<Map<String, Object>> getSection(String sectionId) {
        return sections.get(sectionId);
    }

    /**
     * Returns an unmodifiable view of all sections in this compilation report.
     *
     * @return an unmodifiable map where each key is a section identifier and each value is a list
     *     of key/value pair mappings representing that section's data
     */
    public Map<String, List<Map<String, Object>>> getAllSections() {
        return Collections.unmodifiableMap(sections);
    }
}
