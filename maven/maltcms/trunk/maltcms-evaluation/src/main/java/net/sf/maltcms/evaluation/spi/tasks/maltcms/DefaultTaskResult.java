package net.sf.maltcms.evaluation.spi.tasks.maltcms;

import net.sf.maltcms.evaluation.api.tasks.ITaskResult;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nilshoffmann
 * Date: 10.12.11
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class DefaultTaskResult implements ITaskResult {
    private final List<File> taskInputs;
    private final List<File> taskOutputs;
    public DefaultTaskResult() {
        taskInputs = new LinkedList<File>();
        taskOutputs = new LinkedList<File>();
    }
    
    public static final ITaskResult EMPTY = new EmptyTaskResult();

    @Override
    public List<File> getTaskInputs() {
        return taskInputs;
    }

    @Override
    public List<File> getTaskOutputs() {
        return taskOutputs;
    }

    private static class EmptyTaskResult implements ITaskResult {

        @Override
        public List<File> getTaskInputs() {
            return Collections.emptyList();
        }

        @Override
        public List<File> getTaskOutputs() {
            return Collections.emptyList();
        }
    }
}
