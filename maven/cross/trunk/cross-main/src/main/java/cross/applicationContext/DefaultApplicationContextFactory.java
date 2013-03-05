/* 
 * Cross, common runtime object support system. 
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.applicationContext;

import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author nils
 */
@Slf4j
@Data
public class DefaultApplicationContextFactory {

    private final List<String> applicationContextPaths;
    private final Configuration configuration;

    public ApplicationContext createApplicationContext() throws BeansException {
        AbstractRefreshableApplicationContext context = null;
        try {
            final ConfiguringBeanPostProcessor cbp = new ConfiguringBeanPostProcessor();
            cbp.setConfiguration(configuration);
            context = new FileSystemXmlApplicationContext(applicationContextPaths.toArray(new String[applicationContextPaths.size()]), context);
            context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
                @Override
                public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
                    clbf.addBeanPostProcessor(cbp);
                }
            });
            context.refresh();
        } catch (BeansException e2) {
            throw e2;
        }
        return context;
    }
    
    public ApplicationContext createClassPathApplicationContext() throws BeansException {
        AbstractRefreshableApplicationContext context = null;
        try {
            final ConfiguringBeanPostProcessor cbp = new ConfiguringBeanPostProcessor();
            cbp.setConfiguration(configuration);
            context = new ClassPathXmlApplicationContext(applicationContextPaths.toArray(new String[applicationContextPaths.size()]), context);
            context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
                @Override
                public void postProcessBeanFactory(ConfigurableListableBeanFactory clbf) throws BeansException {
                    clbf.addBeanPostProcessor(cbp);
                }
            });
            context.refresh();
        } catch (BeansException e2) {
            throw e2;
        }
        return context;
    }
}
