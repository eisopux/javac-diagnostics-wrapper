package io.github.wmdietl.diagnostics.json.sarif;

/** The severity of an LSP diagnostic. */
public enum DiagnosticSeverity {
    /** Reports an error. */
    ERROR(1),

    /** Reports a warning. */
    WARNING(2),

    /** Reports an information. */
    INFORMATION(3),

    /** Reports a hint. */
    HINT(4);

    /** The numeric value of this DiagnosticSeverity. */
    public final int value;

    /**
     * Create a new DiagnosticSeverity.
     *
     * @param value the numeric value of this DiagnosticSeverity
     */
    private DiagnosticSeverity(int value) {
        this.value = value;
    }
}
