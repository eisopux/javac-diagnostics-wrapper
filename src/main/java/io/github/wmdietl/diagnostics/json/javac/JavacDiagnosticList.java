package io.github.wmdietl.diagnostics.json.javac;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;
import io.github.wmdietl.diagnostics.json.common.DiagnosticList;
import io.github.wmdietl.diagnostics.json.common.Diagnostic;

public class JavacDiagnosticList extends DiagnosticList {
    public JavacDiagnosticList(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        // When this object is created, delegate construction to the constructor of the super class
        super(diags);
    }

    @Override
    protected List<Diagnostic> convertOrg(List<javax.tools.Diagnostic<? extends JavaFileObject>> diags) {
        List<Diagnostic> processed = new ArrayList<>();
        for(javax.tools.Diagnostic<? extends JavaFileObject> d : diags){
            processed.add(new JavacDiagnostic(d));
        }
        return processed;
    }
}
