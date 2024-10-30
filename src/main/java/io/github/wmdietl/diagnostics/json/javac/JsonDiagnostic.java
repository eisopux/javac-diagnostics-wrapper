package io.github.wmdietl.diagnostics.json.javac;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/** JSON wrapper class. Fields are encoded in the JSON output. */
public class JsonDiagnostic {

    public final String fileUri;
    public final String kind;
    public final long position;
    public final long startPosition;
    public final long endPosition;
    public final long lineNumber;
    public final long columnNumber;
    public final String code;
    public final String message;

    JsonDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
        JavaFileObject file = diagnostic.getSource();
        fileUri = file != null ? file.toUri().toString() : "unknown file";
        kind = diagnostic.getKind().name();
        position = diagnostic.getPosition();
        startPosition = diagnostic.getStartPosition();
        endPosition = diagnostic.getEndPosition();
        lineNumber = diagnostic.getLineNumber();
        columnNumber = diagnostic.getColumnNumber();
        code = diagnostic.getCode();
        message = diagnostic.getMessage(null);
    }
}
