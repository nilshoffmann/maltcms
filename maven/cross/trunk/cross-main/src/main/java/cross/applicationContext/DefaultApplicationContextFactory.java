/*
 *  Copyright (C) 2008-2012 Nils Hoffmann
 *  Nils.Hoffmann A T CeBiTec.Uni-Bielefeld.DE
 *
 *  This file is part of Cross/Maltcms.
 *
 *  Cross/Maltcms is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Cross/Maltcms is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Cross/Maltcms.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package cross.applicationContext;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
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

    private final String[] applicationContextPaths;
    private final Configuration configuration;

    public ApplicationContext createApplicationContext() throws BeansException {
        AbstractRefreshableApplicationContext context = null;
        try {
            final ConfiguringBeanPostProcessor cbp = new ConfiguringBeanPostProcessor();
            cbp.setConfiguration(configuration);
//            context = new ClassPathXmlApplicationContext("/cross-bootstrap.xml");
//            context.refresh();
            context = new FileSystemXmlApplicationContext(
                    applicationContextPaths,context);
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
