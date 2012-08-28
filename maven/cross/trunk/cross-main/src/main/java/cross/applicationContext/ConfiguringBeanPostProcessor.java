/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.applicationContext;

import cross.IConfigurable;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 *
 * @author hoffmann
 */
@Data
public class ConfiguringBeanPostProcessor implements BeanPostProcessor {

    private Configuration configuration;

    @Override
    public Object postProcessBeforeInitialization(Object o, String string) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String string) throws BeansException {
        if (o instanceof IConfigurable) {
//           System.out.println("Configuring bean "+o.getClass().getName());
            ((IConfigurable) o).configure(configuration);
        }
        return o;
    }
}
