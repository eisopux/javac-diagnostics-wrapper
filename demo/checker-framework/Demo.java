/**
 * A trivial, intentionally-flawed fixture for the {@code checkerFrameworkDemo} Gradle task: the
 * Nullness Checker reports {@code dereference.of.nullable} on {@code myObject.toString()}, since
 * {@code myObject} is only assigned a non-null value conditionally. Compiled alongside {@code
 * InterningError.java} (a different checker's finding) and {@code OtherError.java} (a plain,
 * non-Checker-Framework type error), to demonstrate that {@link
 * io.github.eisopux.diagnostics.reporter.SarifReporter} gives each of the three its own, distinct
 * SARIF {@code ruleId}.
 */
public class Demo {
    /**
     * Dereferences {@code myObject} unconditionally, even though it is only assigned a non-null
     * value when {@code args.length > 2}.
     *
     * @param args command-line arguments; only their count matters here
     */
    public static void main(String[] args) {
        Object myObject = null;

        if (args.length > 2) {
            myObject = new Object();
        }
        System.out.println(myObject.toString());
    }
}
