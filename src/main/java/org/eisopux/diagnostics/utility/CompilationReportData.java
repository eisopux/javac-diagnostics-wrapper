package org.eisopux.diagnostics.utility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CompilationReportData {
    private final Map<String, Map<String, Object>> sections = new HashMap<>();

    public void putSection(String sectionId, Map<String, Object> sectionData) {
        sections.put(sectionId, sectionData);
    }

    public Map<String, Object> getSection(String sectionId) {
        return sections.get(sectionId);
    }

    public Map<String, Map<String, Object>> getAllSections() {
        return Collections.unmodifiableMap(sections);
    }
}
