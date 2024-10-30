package io.github.wmdietl.diagnostics.json.common;

import java.util.List;

/** A file plus a list of diagnostics for that file. */
public class FileDiagnostics {
    /** The URI for which diagnostic information is reported. */
    public final String uri;

    /** Diagnostic information items. */
    public final List<Diagnostic> diagnostics;

    /**
     * Create a Fileiagnostics with the given arguments.
     *
     * @param uri the URI for which diagnostic information is reported
     * @param diagnostics diagnostic information items
     */
    public FileDiagnostics(String uri, List<Diagnostic> diagnostics) {
        this.uri = uri;
        this.diagnostics = diagnostics;
    }
}
