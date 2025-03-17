package org.eisopux.diagnostics.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompilationReportData {
    // The value is now an Object so that it can hold any kind of data structure.
    private final Map<String, Object> sections = new HashMap<>();

    public void putSection(String sectionId, Object sectionData) {
        sections.put(sectionId, sectionData);
    }

    public Object getSection(String sectionId) {
        return sections.get(sectionId);
    }

    public Map<String, Object> getAllSections() {
        return Collections.unmodifiableMap(sections);
    }
}
