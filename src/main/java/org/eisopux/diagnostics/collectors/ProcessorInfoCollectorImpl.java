package org.eisopux.diagnostics.collectors;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;

import org.eisopux.diagnostics.core.Collector;
import org.eisopux.diagnostics.core.CompilationTaskBuilder;
import org.eisopux.diagnostics.utility.ProcessorInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;

/**
 * A collector that discovers annotation processors loaded during compilation. Stores the data in
 * {@link ProcessorInfo} objects.
 */
public class ProcessorInfoCollectorImpl implements Collector<ProcessorInfo> {

    private final List<ProcessorInfo> discoveredProcessors = new ArrayList<>();
    private Context javacContext;

    public void registerWithTask(JavaCompiler.CompilationTask task) {
        if (task instanceof JavacTaskImpl) {
            JavacTaskImpl javacTask = (JavacTaskImpl) task;
            this.javacContext = javacTask.getContext();

            javacTask.addTaskListener(
                    new TaskListener() {
                        @Override
                        public void started(TaskEvent e) {
                            if (e.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
                                captureProcessorInfo();
                            }
                        }

                        @Override
                        public void finished(TaskEvent e) {
                            // no-op
                        }
                    });
        }
    }

    @Override
    public void onBeforeCompile(CompilationTaskBuilder builder) {}

    @Override
    public void onAfterCompile() {}

    @Override
    public List<ProcessorInfo> getItems() {
        return discoveredProcessors;
    }

    /**
     * Reflect into the compiler to find annotation processors and store them as {@link
     * ProcessorInfo} objects in {@link #discoveredProcessors}.
     */
    private void captureProcessorInfo() {
        try {
            JavacProcessingEnvironment procEnv = JavacProcessingEnvironment.instance(javacContext);

            Field discoveredProcsField = procEnv.getClass().getDeclaredField("discoveredProcs");
            discoveredProcsField.setAccessible(true);
            Object discoveredProcs = discoveredProcsField.get(procEnv);

            if (discoveredProcs != null) {
                Field procStateListField =
                        discoveredProcs.getClass().getDeclaredField("procStateList");
                procStateListField.setAccessible(true);
                Object procStateListObj = procStateListField.get(discoveredProcs);

                if (procStateListObj instanceof List) {
                    List<?> procStateList = (List<?>) procStateListObj;
                    for (Object processorState : procStateList) {
                        Field processorField =
                                processorState.getClass().getDeclaredField("processor");
                        processorField.setAccessible(true);
                        Object processorObj = processorField.get(processorState);

                        if (processorObj != null) {
                            discoveredProcessors.add(createProcessorInfo(processorObj));
                        } else {
                            discoveredProcessors.add(
                                    new ProcessorInfo(
                                            "[null processor]", "unknown", "unknown", "unknown"));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // TODO: Handle reflection errors
        }
    }

    /** Create a ProcessorInfo from a reflection-based processor object. */
    private ProcessorInfo createProcessorInfo(Object processorObj) {
        String procClassName = processorObj.getClass().getName();
        Package pkg = processorObj.getClass().getPackage();

        String version = null;
        String title = null;
        String vendor = null;

        if (pkg != null) {
            version = pkg.getImplementationVersion();
            title = pkg.getImplementationTitle();
            vendor = pkg.getImplementationVendor();
            if (version == null) {
                version = pkg.getSpecificationVersion();
            }
        }

        if (version == null) version = "unknown";
        if (title == null) title = "unknown";
        if (vendor == null) vendor = "unknown";

        return new ProcessorInfo(procClassName, version, title, vendor);
    }

    /** Returns a copy of the discovered processor info. */
    public List<ProcessorInfo> getDiscoveredProcessors() {
        return new ArrayList<>(discoveredProcessors);
    }
}
