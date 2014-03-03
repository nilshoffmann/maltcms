/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maltcms may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maltcms, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */

package net.sf.maltcms.apps

import groovy.xml.MarkupBuilder
import net.sf.maltcms.groovy.Utils

/**
 *
 * @author Nils Hoffmann
 */
def cli = new CliBuilder(usage:'SpringXmlGenerator')
cli.n(args:1, argName:'classNames', 'use given classes',valueSeparator: (char)',')
def options = cli.parse(args)



def xmlHeader = """
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="
              http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">"""

//<!-- <bean/> definitions here -->


//<bean id="myField"
//        class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean">
//  <property name="staticField" value="java.sql.Connection.TRANSACTION_SERIALIZABLE"/>
//</bean>

//<property name="commands">
//<list>
//<ref bean="csvAnchorReader" />
//<ref bean="defaultVarLoader" />
//<ref bean="massFilter" />
//<!--                <ref bean="ticHeatmapCoplotBeforeAlignment"/>-->
//<!--                <ref bean="eicHeatmapCoplotBeforeAlignment"/>-->
//<ref bean="denseArrayProducer" />
//<ref bean="ticPeakFinder" />
//<ref bean="peakCliqueAlignment" /><!--
//<ref bean="pairwiseDistanceCalculator" />
//<ref bean="centerStarAlignment" />
//<ref bean="chromatogramWarp" />
//<ref bean="ticHeatmapCoplotAfterAlignment"/>
//<ref bean="eicHeatmapCoplotAfterAlignment"/>-->
//</list>
//</property>   



def xmlFooter = """
</beans>"""

def beanFragments = []
def springBeans = [:]
GroovyClassLoader gcl = new GroovyClassLoader()
for(def className : cli.n) {
    def services = []
    services.addAll(Lookup.getDefault().lookupAll())
    if(services.isEmpty()) {
        //instantiate bean manually
        Class c = Class.forName(className)
        Object obj = c.newInstance()
        services << obj
    }
    services.each{
        service->
        def beanName = Utils.toPropertyName(service.getClass().getSimpleName())
        int beanCnt = 0
        while(springBeans.containsKey(beanName)) {
            beanCnt++
            beanName=beanName+beanCnt
        }
        SpringBean bean = [id:beanName,typeClass:service.getClass()]
    }
}

def exploreChildren = {
    parentObj, level, map, prefix ->
    parentObj.getMetaPropertyValues().each{ metaProperty ->
        SpringBean springBean = new SpringBean(
            id:metaProperty.name, 
            typeClass:metaProperty.type,
            value:metaProperty.value
        )
        
        if(value) {
            //
            if(simpleTypes.contains(type)) {
                map[prefix+"."+name] = value
            }else{
                exploreChildren.curry(value, level+1, map, prefix+"."+name)
            }
        }else{
            Lookup.getDefault().lookupAll().each{
                service->exploreChildren.curry(service, level+1, map, prefix+"."+name)
            }
            
        }
    }
    return map
}

public class Ref {
    
}

public class Property {
    
}

public class Bean {
    String id
    Class beanClass
    def props = [:]
    
    public StringWriter buildTag() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        
        
        
        return writer
    }
}

public class SpringBean {
    String id
    Class typeClass
    def props = [:]
    
    public PrintStream createXml(Map beans) {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)
        builder.bean(id:id,class:typeClass.getName()){
            props.each {
                key,value -> 
                SpringBean propertyBean = key
                Object beanValue = value
                if(beanValue) {
                    
                    if(isSimpleType(typeClass)) {
                        property(name:key,value:value)
                    }else if(isListType()) {
                        property(name:key) {    
                            value.each{ member-> }
                        }
                    }else if(isSetType()) {
                        property(name:key) {
                            
                            value.each{ member->}
                        }
                    
                    }else if(isMapType()) {
                    
                    }else if(isArrayType()) {
                    
                    }
                }else{
                    if(beans.containsKey(propertyBean.id)) {
                        
                    }
                    
                }
            }
            
            simpleMembers.each{ prop -> builder.property(id:prop.key,value:prop.value)}
            springBeanMembers.each{ bean -> builder.ref(bean:bean.id)}
        }
    }
    
    public static final simpleTypes = [
        Boolean.class,
        Character.class,
        Byte.class,
        Short.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Void.class,
        String.class
    ] as Set
    
    public static boolean isListType(Class typeClass) {
        return typeClass instanceof List
    }
    
    public static boolean isSetType(Class typeClass) {
        return typeClass instanceof Set
    }
    
    public static boolean isMapType(Class typeClass) {
        return typeClass instanceof Map
    }
    
    public static boolean isArrayType(Class typeClass) {
        return typeClass.isArray()
    }
    
    public static boolean isSimpleType(Class typeClass) {
        return simpleTypes.contains(typeClass) || typeClass.isPrimitive()
    }
}

public class CollectionProperty {
    
}

