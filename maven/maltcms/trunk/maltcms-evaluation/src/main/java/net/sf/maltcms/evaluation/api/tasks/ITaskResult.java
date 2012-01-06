package net.sf.maltcms.evaluation.api.tasks;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nilshoffmann
 * Date: 10.12.11
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public interface ITaskResult extends Serializable {
    List<File> getTaskInputs();
    List<File> getTaskOutputs();
}
