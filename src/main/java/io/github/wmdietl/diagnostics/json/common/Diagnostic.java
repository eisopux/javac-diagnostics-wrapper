package io.github.wmdietl.diagnostics.json.common;

import javax.tools.JavaFileObject;

// Interface for multiple types of Diagnostics (currently: javac, lsp, sarif)
public abstract class Diagnostic {
    /**
     * Each different types of diagnostic that derive this base class should implement the make method
     * Convert from a javac standard diagnostics to the target self-defined diagnostic class
     * 
     * @param diagnostic standard javac diagnostics
     */
    public abstract Diagnostic make(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic);
}
