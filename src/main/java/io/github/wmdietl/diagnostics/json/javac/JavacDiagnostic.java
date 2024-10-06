package io.github.wmdietl.diagnostics.json.javac;

import javax.tools.JavaFileObject;
import io.github.wmdietl.diagnostics.json.common.Diagnostic;

public class JavacDiagnostic extends Diagnostic {

    public final String fileUri;
    public final String kind;
    public final long position;
    public final long startPosition;
    public final long endPosition;
    public final long lineNumber;
    public final long columnNumber;
    public final String code;
    public final String message;

    public JavacDiagnostic(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
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
