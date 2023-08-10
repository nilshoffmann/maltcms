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
package net.sf.maltcms.apps.util;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.AFragmentCommand;
import cross.datastructures.workflow.WorkflowSlot;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.openide.util.Lookup;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author Nils Hoffmann
 */

public class FragmentCommandDocGenerator {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FragmentCommandDocGenerator.class);

    public void generateDocuments(Configuration cfg) {
        File outputDir = new File(cfg.getString("output.basedir", "."), "documentation/commands/");
        outputDir.mkdirs();
        log.info("Creating markdown documents below {}", outputDir);
        Collection<? extends AFragmentCommand> commands = Lookup.getDefault().lookupAll(AFragmentCommand.class);
        Map<WorkflowSlot, Set<AFragmentCommand>> slotToCommandMap = new LinkedHashMap<WorkflowSlot, Set<AFragmentCommand>>();
        for (AFragmentCommand command : commands) {
            Set<AFragmentCommand> commandsInSlot = slotToCommandMap.get(command.getWorkflowSlot());
            if (commandsInSlot == null) {
                commandsInSlot = new LinkedHashSet<>();
                slotToCommandMap.put(command.getWorkflowSlot(), commandsInSlot);
            }
            commandsInSlot.add(command);
        }
        File globalIndexMdFile = new File(outputDir, "index.md");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(globalIndexMdFile))) {
            bw.write("<h1>Maltcms Commands</h1>");
            bw.newLine();
            List<WorkflowSlot> slots = new ArrayList<WorkflowSlot>(slotToCommandMap.keySet());
            Collections.sort(slots, new Comparator<WorkflowSlot>() {

                @Override
                public int compare(WorkflowSlot o1, WorkflowSlot o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            for (WorkflowSlot slot : slots) {
                bw.write("<h2>" + slot.toString() + " commands</h2>");
                bw.newLine();
                File slotDir = new File(outputDir, slot.toString().toLowerCase());
                slotDir.mkdirs();
                List<AFragmentCommand> commandsList = new ArrayList<>(slotToCommandMap.get(slot));
                Collections.sort(commandsList, new Comparator<AFragmentCommand>() {

                    @Override
                    public int compare(AFragmentCommand o1, AFragmentCommand o2) {
                        return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                    }
                });
                for (AFragmentCommand command : commandsList) {
                    File mdFile = new File(slotDir, getTargetFile(command));
                    bw.write("* [" + command.getClass().getSimpleName() + "](./" + slot.toString().toLowerCase() + "/" + mdFile.getName().replace(".md", ".html") + ")");
                    bw.newLine();
                }
                bw.newLine();
            }
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            log.error("Caught exception while creating file " + globalIndexMdFile + ":", ex);
        }
        for (WorkflowSlot slot : slotToCommandMap.keySet()) {
            File slotDir = new File(outputDir, slot.toString().toLowerCase());
            slotDir.mkdirs();
            File indexMdFile = new File(slotDir, "index.md");
            log.info("Creating index file {}", indexMdFile);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(indexMdFile))) {
                bw.write("<h1>" + slot.toString() + " commands</h1>");
                bw.newLine();
                List<AFragmentCommand> commandsList = new ArrayList<>(slotToCommandMap.get(slot));
                Collections.sort(commandsList, new Comparator<AFragmentCommand>() {

                    @Override
                    public int compare(AFragmentCommand o1, AFragmentCommand o2) {
                        return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                    }
                });
                for (AFragmentCommand command : commandsList) {
                    File mdFile = new File(slotDir, getTargetFile(command));
                    bw.write("* [" + command.getClass().getSimpleName() + "](./" + mdFile.getName().replace(".md", ".html") + ")");
                    bw.newLine();
                }
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                log.error("Caught exception while creating file " + indexMdFile + ":", ex);
            }
            List<AFragmentCommand> commandsList = new ArrayList<>(slotToCommandMap.get(slot));
            Collections.sort(commandsList, new Comparator<AFragmentCommand>() {

                @Override
                public int compare(AFragmentCommand o1, AFragmentCommand o2) {
                    return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                }
            });
            for (AFragmentCommand command : commandsList) {
                File commandDir = new File(outputDir, slot.toString().toLowerCase());
                commandDir.mkdirs();
                File mdFile = new File(commandDir, getTargetFile(command));
                log.info("Creating file {}", mdFile);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(mdFile))) {
                    bw.write("<h1>" + command.getClass().getSimpleName() + "</h1>");
                    bw.newLine();
                    bw.write("**Class**: `" + command.getClass().getCanonicalName() + "`  ");
                    bw.newLine();
                    bw.write("**Workflow Slot**: " + command.getWorkflowSlot() + "  ");
                    bw.newLine();
                    bw.write("**Description**: " + command.getDescription() + "  ");
                    bw.newLine();
                    bw.newLine();
                    bw.write("---");
                    bw.newLine();
                    bw.newLine();
                    bw.write("<h2>Variables</h2>");
                    bw.newLine();
                    bw.write("<h3>Required</h3>");
                    bw.newLine();
                    for (String var : AnnotationInspector.getRequiredVariables(command)) {
                        bw.write("\t" + var);
                        bw.newLine();
                    }
                    bw.newLine();
                    bw.write("<h3>Optional</h3>");
                    bw.newLine();
                    for (String var : AnnotationInspector.getOptionalRequiredVariables(command)) {
                        bw.write("\t" + var);
                        bw.newLine();
                    }
                    bw.newLine();
                    bw.write("<h3>Provided</h3>");
                    bw.newLine();
                    for (String var : AnnotationInspector.getProvidedVariables(command)) {
                        bw.write("\t" + var);
                        bw.newLine();
                    }
                    bw.newLine();
                    bw.newLine();
                    bw.write("---");
                    bw.newLine();
                    bw.newLine();
                    bw.write("<h2>Configurable Properties</h2>");
                    bw.newLine();
                    ArrayList<String> configKeys = new ArrayList<>(AnnotationInspector.getRequiredConfigKeys(command));
                    Collections.sort(configKeys);
                    for (String configKey : configKeys) {
                        if (!configKey.startsWith("var.")) {
                            String description = AnnotationInspector.getDescriptionFor(command.getClass(), configKey);
                            String defaultValue = AnnotationInspector.getDefaultValueFor(command.getClass(), configKey);
                            String propertyName = configKey.substring(configKey.lastIndexOf(".") + 1);
                            if (defaultValue.isEmpty()) {
                                try {
                                    PropertyDescriptor propDescr = BeanUtils.getPropertyDescriptor(command.getClass(), propertyName);
                                    if (propDescr != null) {
                                        bw.write("**Name**: `" + propertyName + "`  ");
                                        bw.newLine();
                                        bw.write("**Default Value**: `" + propDescr.getReadMethod().invoke(command) + "`  ");
                                    }
                                } catch (SecurityException ex) {
                                    log.warn("Security exception while trying to access method '" + configKey + "'", ex);
                                } catch (IllegalArgumentException ex) {
                                    log.warn("Illegal argument exception while trying to access method '" + configKey + "'", ex);
                                } catch (IllegalAccessException ex) {
                                    log.warn("Illegal access exception while trying to access method '" + configKey + "'", ex);
                                } catch (InvocationTargetException ex) {
                                    log.warn("Invocation target exception while trying to invoke method '" + configKey + "'", ex);
                                }
                            } else {
                                bw.write("**Name**: `" + propertyName + "`  ");
                                bw.newLine();
                                bw.write("**Default Value**: `" + defaultValue + "`  ");
                            }
                            bw.newLine();
                            bw.write("**Description**:  ");
                            bw.newLine();
                            bw.write("" + description + "  ");
                            bw.newLine();
                            bw.newLine();
                            bw.write("---");
                            bw.newLine();
                            bw.newLine();
                        }
                    }
                    bw.newLine();
                    bw.flush();
                } catch (IOException ex) {
                    log.error("Caught exception while creating file " + mdFile + ":", ex);
                }
            }
        }
    }

    private String getTargetFile(AFragmentCommand command) {
        return "" + command.getClass().getSimpleName() + ".md";
    }

    private File getTargetDir(AFragmentCommand command, String... packagePrefix) {
        for (String str : packagePrefix) {
            if (command.getClass().getPackage().getName().startsWith(str)) {
                return new File(command.getClass().getPackage().getName().substring(str.length()).replaceAll("\\.", "/"));
            }
        }
        return new File(command.getClass().getPackage().getName().replaceAll("\\.", "/"));
    }

}
