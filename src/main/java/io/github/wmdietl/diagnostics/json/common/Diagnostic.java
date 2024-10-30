package io.github.wmdietl.diagnostics.json.common;

import java.util.ArrayList;
import java.util.List;

/** Represents a diagnostic, such as a compiler error or warning. */
public class Diagnostic {
    /** The range at which the message applies. */
    public final Range range;

    /**
     * The diagnostic's severity. Can be omitted. If omitted it is up to the client to interpret
     * diagnostics as error, warning, info or hint.
     */
    public final Integer severity;

    /** The diagnostic's code, which might appear in the user interface. */
    public final String code;

    /**
     * A human-readable string describing the source of this diagnostic, e.g. 'typescript' or 'super
     * lint'.
     */
    public final String source;

    /** The diagnostic's message. */
    public final String message;

    /** Additional metadata about the diagnostic. */
    public final List<Integer> tags;

    /**
     * Create a Diagnostic using all arguments.
     *
     * @param range the range at which the message applies
     * @param severity the diagnostic's severity
     * @param code the diagnostic's code, which might appear in the user interface
     * @param source A human-readable string describing the source of this diagnostic, e.g.
     *     'typescript' or 'super lint'.
     * @param message the diagnostic's message
     * @param tags additional metadata about the diagnostic
     */
    public Diagnostic(
            Range range,
            Integer severity,
            String code,
            String source,
            String message,
            List<Integer> tags) {
        this.range = range;
        this.severity = severity;
        this.code = code;
        this.source = source;
        this.message = message;
        this.tags = (tags == null ? null : new ArrayList<>(tags));
    }
}
