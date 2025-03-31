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
