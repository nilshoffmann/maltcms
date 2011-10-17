/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cross.applicationContext;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author nils
 */
@Data
public class DefaultApplicationContextFactory {

    private final String[] applicationContextPaths;

    public ApplicationContext createApplicationContext() throws BeansException {
        ApplicationContext context = null;
        try {
            context = new FileSystemXmlApplicationContext(
                    applicationContextPaths);
        } catch (BeansException e2) {
            context = new ClassPathXmlApplicationContext(applicationContextPaths);
        }
        return context;
    }
}
