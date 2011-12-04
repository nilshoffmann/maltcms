/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.maltcms.datastructures;

import java.util.List;

/**
 *
 * @author nilshoffmann
 */
public interface IElementProvider<T> {
    
    int size();
    T get(int i);
    List<T> get(int start, int stop);
    void reset();
    
}
