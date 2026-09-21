/**
 * A trivial, intentionally-flawed fixture for the {@code checkerFrameworkDemo} Gradle task: the
 * Nullness Checker reports {@code dereference.of.nullable} on {@code myObject.toString()}, since
 * {@code myObject} is only assigned a non-null value conditionally. Compiled alongside {@code
 * OtherError.java}, whose plain (non-Checker-Framework) type error demonstrates that {@link
 * io.github.eisopux.diagnostics.reporter.SarifReporter} gives each kind of finding its own,
 * distinct SARIF {@code ruleId}.
 */
public class Demo {
    public static void main(String[] args) {
        Object myObject = null;

        if (args.length > 2) {
            myObject = new Object();
        }
        System.out.println(myObject.toString());
    }
}
