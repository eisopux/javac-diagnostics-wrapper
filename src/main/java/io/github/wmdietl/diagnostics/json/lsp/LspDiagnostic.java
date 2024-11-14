package io.github.wmdietl.diagnostics.json.lsp;

import java.util.List;

import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import io.github.wmdietl.diagnostics.common.Range;

/** A file plus a list of diagnostics for that file. Define one complete entry in json output */
public class LspDiagnostic {
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
    public LspDiagnostic(String uri, List<Diagnostic> diagnostics) {
        this.uri = uri;
        this.diagnostics = diagnostics;
    }

    /** Represents a diagnostic, such as a compiler error or warning. */
    public static class Diagnostic {
        /** The range at which the message applies. */
        public final Range range;

        /**
         * The diagnostic's severity. Can be omitted. If omitted it is up to the client to interpret
         * diagnostics as error, warning, info or hint.
         */
        public final int severity;

        /** The diagnostic's code, which might appear in the user interface. */
        public final String code;

        /**
         * A human-readable string describing the source of this diagnostic, e.g. 'typescript' or
         * 'super lint'.
         */
        public final String source;

        /** The diagnostic's message. */
        public final String message;

        /** Additional metadata about the diagnostic. */
        public final List<Integer> tags;

        /** Create a Diagnostic using standard javac diagnostic. */
        public Diagnostic(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
            // Convert from javac error locations to self-defined range
            this.range =
                    new Range(
                            diagnostic.getLineNumber(), diagnostic.getColumnNumber(),
                            diagnostic.getStartPosition(), diagnostic.getEndPosition());
            // Convert from javac severity to LSP severity
            this.severity = mapSeverity(diagnostic.getKind()).severity;
            this.code = diagnostic.getCode();
            this.source = getClass().getCanonicalName();
            this.message = diagnostic.getMessage(null);
            this.tags = null;
        }

        /**
         * Convert from javac diagnostic Kind to DiagnosticSeverity.
         *
         * @param kind the severity of the diagnostic produced by javac
         */
        private static LspDiagnosticSeverity mapSeverity(final Kind kind)
                throws IllegalArgumentException {
            switch (kind) {
                case ERROR:
                    return LspDiagnosticSeverity.ERROR;
                case WARNING:
                case MANDATORY_WARNING:
                    return LspDiagnosticSeverity.WARNING;
                case NOTE:
                case OTHER:
                    return LspDiagnosticSeverity.INFORMATION;
                default:
                    throw new IllegalArgumentException("Unexpected diagnostic kind");
            }
        }
    }
}
