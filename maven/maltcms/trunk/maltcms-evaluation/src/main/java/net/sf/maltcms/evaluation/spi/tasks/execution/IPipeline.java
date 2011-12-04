/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.evaluation.spi.tasks.execution;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author nilshoffmann
 */
public interface IPipeline<T extends Serializable> extends Callable<List<T>>, Serializable {
   
}
