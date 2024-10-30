package io.github.wmdietl.diagnostics.json.common;

import javax.tools.Diagnostic.Kind;

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

    /**
     * Convert from javac diagnostic Kind to DiagnosticSeverity
     * 
     * @param kind the severity of the diagnostic produced by javac
     */
    public static DiagnosticSeverity convert(final Kind kind) throws IllegalArgumentException {
        switch (kind) {
            case ERROR:
                return DiagnosticSeverity.ERROR;
            case WARNING:
            case MANDATORY_WARNING:
                return DiagnosticSeverity.WARNING;
            case NOTE:
            case OTHER:
                return DiagnosticSeverity.INFORMATION;
            default:
                throw new IllegalArgumentException("Unexpected diagnostic kind");
        }
    }
}
