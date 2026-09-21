/**
 * A third, unrelated fixture for the {@code checkerFrameworkDemo} Gradle task, compiled alongside
 * {@code Demo.java} and {@code InterningError.java}: this plain type mismatch is a genuine javac
 * diagnostic ({@code compiler.err.prob.found.req}), not a Checker Framework finding at all, to
 * show that {@code SarifReporter} gives it its own distinct SARIF {@code ruleId} -- the
 * diagnostic's own code -- separate from either checker's finding in the same run.
 *
 * <p>This bug must live in its own file rather than alongside {@code Demo.java}'s or {@code
 * InterningError.java}'s: a plain attribution error in a class prevents the Checker Framework from
 * running its own analysis on that same class, so combining it with either checker's bug would
 * silently lose that checker's finding.
 */
public class OtherError {
    /** Assigns a {@code String} to an {@code int} variable, which does not type-check. */
    static void m() {
        int badNumber = "not a number";
    }
}
