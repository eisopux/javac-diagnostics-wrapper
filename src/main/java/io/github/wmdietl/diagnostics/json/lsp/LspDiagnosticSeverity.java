package io.github.wmdietl.diagnostics.json.lsp;

/**
 * The severity of an LSP diagnostic.
 *
 * <p>For detailed reference of how each LSP severity is defined, please refer to the link below:
 * https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/
 */
public enum LspDiagnosticSeverity {
    /** Reports an error. */
    ERROR(1),

    /** Reports a warning. */
    WARNING(2),

    /** Reports an information. */
    INFORMATION(3),

    /** Reports a hint. */
    HINT(4);

    /** The numeric value of this DiagnosticSeverity. */
    public final int severity;

    /**
     * Create a new DiagnosticSeverity.
     *
     * @param value the numeric value of this DiagnosticSeverity
     */
    private LspDiagnosticSeverity(int severity) {
        this.severity = severity;
    }
}
