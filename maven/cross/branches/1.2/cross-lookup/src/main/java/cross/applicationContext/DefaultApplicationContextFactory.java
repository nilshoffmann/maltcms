/**
 * Copyright (C) 2008-2011 Nils Hoffmann Nils.Hoffmann A T
 * CeBiTec.Uni-Bielefeld.DE
 *
 * This file is part of Cross/Maltcms.
 *
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * 
 *
 * $Id$
 */

package cross.applicationContext;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apidesign.spring.SpringAndLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author NilsHoffmann@CeBiTec.Uni-Bielefeld.DE
 */
@Slf4j
@ServiceProvider(service=IApplicationContextFactory.class)
public class DefaultApplicationContextFactory implements IApplicationContextFactory {
    
    @Override
    public ApplicationContext createApplicationContext(String[] applicationContextPaths) throws BeansException {
        ApplicationContext servicesContext = SpringAndLookup.create(
                Lookup.getDefault(), "java.extensions");
        AbstractApplicationContext context = null;
        try {
            context = new FileSystemXmlApplicationContext(applicationContextPaths,servicesContext);
            log.info("Using context as defined in file system resources: {}",Arrays.toString(applicationContextPaths));
        } catch (BeansException e2) {
            context = new ClassPathXmlApplicationContext(applicationContextPaths,servicesContext);
            log.info("Using context as defined in class path resources: {}",Arrays.toString(applicationContextPaths));
        }
        context.registerShutdownHook();
        return context;
    }
}
