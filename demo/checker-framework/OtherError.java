/**
 * A second, unrelated fixture for the {@code checkerFrameworkDemo} Gradle task, compiled alongside
 * {@code Demo.java}: this plain type mismatch is a genuine javac diagnostic
 * ({@code compiler.err.prob.found.req}), not a Checker Framework finding, to show that
 * {@code SarifReporter} gives it its own distinct SARIF {@code ruleId} -- the diagnostic's own
 * code -- separate from the Nullness Checker's {@code dereference.of.nullable} finding from
 * {@code Demo.java} in the same run.
 *
 * <p>This bug must live in its own file rather than alongside {@code Demo.java}'s: a plain
 * attribution error in a class prevents the Checker Framework from running its own analysis on
 * that same class, so combining both bugs into one file would silently lose the Nullness Checker
 * finding this demo exists to show.
 */
public class OtherError {
    static void m() {
        int badNumber = "not a number";
    }
}
