package io.github.wmdietl.diagnostics.json.sarif;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Tool;

class Sarif {
    private Tool tool;
    private List<FileDiagnostics> results;
    private String version;
    private String schema;

    Sarif(Driver driver) {
        this.tool = new Tool(driver);
        this.results = new ArrayList<FileDiagnostics>();
        this.version = "0.1";
        this.schema = "";
    }

    void addResults(List<FileDiagnostics> customResults) {
        results.addAll(customResults);
    }

    String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public void downloadAsSARIF() {
        String json = toJson();
        String fileName = "output.sarif"; // Name of the SARIF file
        String filePath = fileName; // SARIF file will be created in the current directory

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(json);
            System.out.println("SARIF file downloaded successfully: " + filePath);
        } catch (IOException e) {
            System.err.println("Error downloading SARIF file: " + e.getMessage());
        }
    }

    private static class Tool {
        private Driver driver;

        Tool(Driver driver) {
            this.driver = driver;
        }
    }

    public static class Driver {
        private String name;
        private String informationUri;
        private List<Rule> rules;

        Driver(String name, String informationUri, List<Rule> rules) {
            this.name = name;
            this.informationUri = informationUri;
            this.rules = rules;
        }
    }

    public static class Rule {
        private String id;
        private String name;
        private Help help;
        private String helpUri;

        public Rule(String id, String name, String helpstr, String helpUri) {
            this.id = id;
            this.name = name;
            this.help = new Help(helpstr);
            this.helpUri = helpUri;
        }
    }

    public static class Help {
        private String text;

        public Help(String text) {
            this.text = text;
        }
    }
}
