/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package maltcms.commands.filters.array;

import java.util.List;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;

/**
 *
 * @author nilshoffmann
 */
public class BatchFilter {

    public static Array applyFilters(Array a, List<AArrayFilter> filters) {
        Array b = a;
        for (AArrayFilter filter : filters) {
            LoggerFactory.getLogger(BatchFilter.class).info("Applying Filter: {}", filter.getClass().getName());
            b = filter.apply(b);
        }
        return b;
    }
}
