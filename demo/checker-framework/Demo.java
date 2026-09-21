/**
 * A trivial, intentionally-flawed fixture for the {@code checkerFrameworkDemo} Gradle task: the
 * Nullness Checker reports {@code dereference.of.nullable} on {@code myObject.toString()}, since
 * {@code myObject} is only assigned a non-null value conditionally.
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
