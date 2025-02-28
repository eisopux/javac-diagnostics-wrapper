package org.eisopux.diagnostics.utility;

/**
 * Simple DTO for storing information
 * about an annotation processor discovered during compilation.
 */
public class ProcessorInfo {

    private final String className;
    private final String version;
    private final String title;
    private final String vendor;

    /**
     * Constructs a ProcessorInfo.
     *
     * @param className the fully qualified class name of the processor
     * @param version   the implementation or specification version, if available
     * @param title     the package implementation title, if available
     * @param vendor    the package implementation vendor, if available
     */
    public ProcessorInfo(String className, String version, String title, String vendor) {
        this.className = className;
        this.version = version;
        this.title = title;
        this.vendor = vendor;
    }

    public String getClassName() {
        return className;
    }

    public String getVersion() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public String getVendor() {
        return vendor;
    }

    @Override
    public String toString() {
        return "ProcessorInfo{" +
                "className='" + className + '\'' +
                ", version='" + version + '\'' +
                ", title='" + title + '\'' +
                ", vendor='" + vendor + '\'' +
                '}';
    }
}
