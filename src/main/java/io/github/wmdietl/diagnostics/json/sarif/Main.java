package io.github.wmdietl.diagnostics.json.sarif;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

import de.jcup.sarif_2_1_0.SarifSchema210ImportExportSupport;
import de.jcup.sarif_2_1_0.model.*;
import de.jcup.sarif_2_1_0.model.SarifSchema210.Version;

import de.jcup.sarif_2_1_0.model.Result.Level;
import io.github.wmdietl.diagnostics.JavacDiagnosticsWrapper;


/** Wrapper around javac to output diagnostics as JSON, in the Sarif format. */
public class Main extends JavacDiagnosticsWrapper {
    public static void main(String[] args) {
        new Main().run(args);
    }

    /** Serialize the diagnostics using the Sarif format. */
    @Override
    protected void processDiagnostics(
        boolean result, List<javax.tools.Diagnostic<? extends JavaFileObject>> diagnostics, String processor) {
        // Mapping from unique URIs to the diagnostics for that URI
        Map<String, List<Diagnostic>> fileDiagnostics = new HashMap<>();
        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diagnostics) {
            JavaFileObject file = d.getSource();
            String source = file != null ? file.toUri().toString() : "unknown file";
            if (!fileDiagnostics.containsKey(source)) {
                fileDiagnostics.put(source, new ArrayList<>());
            }
            fileDiagnostics.get(source).add(convert(d));
        }

        // Convert to FileDiagnostics for JSON identifiers
        List<FileDiagnostics> jsonDiagnostics = new ArrayList<>();
        for (Map.Entry<String, List<Diagnostic>> entry : fileDiagnostics.entrySet()) {
            jsonDiagnostics.add(new FileDiagnostics(entry.getKey(), entry.getValue()));
        }

        if (!processor.equals("Default")){

        }

        //create a sarif report
        SarifSchema210 sarifReport = convertToSarif(jsonDiagnostics, processor);
        SarifSchema210ImportExportSupport exportReport = new SarifSchema210ImportExportSupport();
        
        // Create a File object
        try {
            String filePath = "../output.sarif";
            File file = new File(filePath);
            exportReport.toFile(sarifReport, file);
        } catch (IOException e) {
            System.err.println("Error occurred while exporting the file: " + e.getMessage());
        }

    }


    private SarifSchema210 convertToSarif(List<FileDiagnostics> jsonDiagnostics, String processor) {
        //create sarif object
        SarifSchema210 sarif = new SarifSchema210();

        //set URI
        String uri_str = "http://json.schemastore.org/sarif-2.1.0-rtm.4";
        URI uri = URI.create(uri_str);
        sarif.set$schema(uri);
        sarif.setVersion(Version._2_1_0);

        ArrayList<Run> runs = new ArrayList<Run>();

        ToolComponent driver = new ToolComponent();
        driver.setName("java-diagnostics-wrapper");

        uri_str = "https://github.com/eisopux/javac-diagnostics-wrapper";
        uri = URI.create(uri_str);
        driver.setInformationUri(uri);  

        List<Result> results = new ArrayList();

        Set<ReportingDescriptor> rules = new HashSet();

        for (int i=0; i<jsonDiagnostics.size(); i++) {

            List<Diagnostic> diagnostics = jsonDiagnostics.get(i).diagnostics;
            String diagnostic_uri = jsonDiagnostics.get(i).uri;

            for (int x=0; x<diagnostics.size(); x++){
                //create result
                Result result = new Result();

                //destructure diagnostic
                Range range = diagnostics.get(x).range;
                Integer severity = diagnostics.get(x).severity;
                String code = diagnostics.get(x).code;
                String message = diagnostics.get(x).message;

                //set level
                switch (severity) {
                    case 1:
                        result.setLevel(Level.ERROR);
                        break;
                    case 2:
                        result.setLevel(Level.WARNING);
                        break;
                    case 3:
                        result.setLevel(Level.NOTE);
                        break;
                    default:
                        result.setLevel(Level.NONE);
                }

                //set message
                Message msg = new Message();
                msg.setText(message);

                //create location object
                List<Location> locations = new ArrayList();

                Location loc = new Location();

                //add physical location
                PhysicalLocation physLoc = new PhysicalLocation();

                //set artifact location with uri
                ArtifactLocation artifactLoc = new ArtifactLocation();
                artifactLoc.setUri(diagnostic_uri);

                //set region 
                Region reg = new Region();
                //set start region
                reg.setStartLine(range.start.line);
                reg.setStartColumn(range.start.character);

                //set end region
                reg.setEndLine(range.end.line);
                reg.setEndColumn(range.end.character);

                //add to physical location
                physLoc.setArtifactLocation(artifactLoc);
                physLoc.setRegion(reg);

                loc.setPhysicalLocation(physLoc);

                locations.add(loc);

                //set locations
                result.setLocations(locations);

                //create rule
                ReportingDescriptor rule = new ReportingDescriptor();
                rule.setId(code);
                //add to set of all rules
                rules.add(rule);
                
                //set rule ID for result
                String ruleId = code;
                result.setRuleId(ruleId);

                //add message to result
                result.setMessage(msg);

                //add result to list of all results
                results.add(result);
            }
        }

        //add rules to driver
        driver.setRules(rules);

        //create tool and set driver
        Tool tool1 = new Tool();
        tool1.setDriver(driver);

        //if using external checker, add as property
        if (!"Default".equals(processor)){
            PropertyBag property = new PropertyBag();

            Set<String> props = new HashSet();
            props.add(processor);

            property.setTags(props);

            //add to tool
            tool1.setProperties(property);
        }

        //create one run and add the tool
        Run run1 = new Run();
        run1.setTool(tool1);

        //add results to run
        run1.setResults(results);

        //add to list of runs 
        runs.add(run1);

        //add runs to the sarif object
        sarif.setRuns(runs);

        return sarif;
    }

    private Diagnostic convert(javax.tools.Diagnostic<? extends JavaFileObject> diagnostic) {
        DiagnosticSeverity severity;
        switch (diagnostic.getKind()) {
            case ERROR:
                severity = DiagnosticSeverity.ERROR;
                break;
            case WARNING:
            case MANDATORY_WARNING:
                severity = DiagnosticSeverity.WARNING;
                break;
            case NOTE:
            case OTHER:
                severity = DiagnosticSeverity.INFORMATION;
                break;
            default:
                throw new IllegalArgumentException("Unexpected diagnostic kind in: " + diagnostic);
        }

        int line = (int) diagnostic.getLineNumber();
        int column = (int) diagnostic.getColumnNumber();
        Range range;
        if (line < 1 || column < 1) {
            // Use beginning of document for invalid locations
            range = new Range(Position.START, Position.START);
        } else {
            // javac is 1-based whereas LSP is 0-based
            Position start = new Position(line - 1, column - 1);
            Position end =
                    new Position(
                            line - 1,
                            (int)
                                    (column
                                            - 1
                                            + diagnostic.getEndPosition()
                                            - diagnostic.getStartPosition()));
            range = new Range(start, end);
        }

        return new Diagnostic(
                range,
                severity.value,
                diagnostic.getCode(),
                this.getClass().getCanonicalName(),
                diagnostic.getMessage(null),
                null);
    }
}
