/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.ap;

import groovy.swing.SwingBuilder
import groovy.transform.Canonical
import groovy.beans.Bindable
import java.awt.BorderLayout as BL
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import javax.swing.JFrame
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.text.DefaultFormatterFactory
import javax.swing.text.NumberFormatter
import java.util.concurrent.TimeUnit
import java.nio.charset.Charset
import javax.swing.AbstractAction
import java.awt.event.ActionEvent
import java.text.SimpleDateFormat
import javax.swing.JFileChooser
import java.awt.Component
import java.awt.Container

def osName = System.properties['os.name'].toLowerCase()
def laf = "system"
if(osName.contains("linux")) {
	laf ="nimbus"
}

UserProperties userProps = new UserProperties()
APProperties props = new APProperties(wdir:new File(System.getProperty("ap.home")))

NumberFormatter nf = new NumberFormatter(new DecimalFormat("#0.0000"))
nf.setAllowsInvalid(true)
nf.setOverwriteMode(true)
DefaultFormatterFactory df = new DefaultFormatterFactory(nf,nf,nf)

MaltcmsExecution execution = null
SwingBuilder swing = new SwingBuilder()
swing.lookAndFeel(laf)

public void setMode(String mode, SwingBuilder swing, APProperties props) {
	println "Using mode $mode"
	props.mr.activePanels["ap"].each{
		apit -> enableComponent(swing."$apit",false)
	}
	props.mr.activePanels[mode].each{
		apit -> enableComponent(swing."$apit",true )
	}
//	swing.pipelineMode.value = mode
	props.mr.pipelineMode = mode
	props.mr.pipelineFile = new File(System.getProperty("ap.home"),"cfg/${mode}.properties")
}

public void enableComponent(Component comp, boolean state) {
	comp.enabled = state
	if(comp instanceof Container) {
		Container cont = (Container)comp
		cont.getComponents().each {
			child -> enableComponent(child, state)
		}
	}
}

def importTab = swing.panel(constraints: BL.CENTER, id: "importTab", name: "Import", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Input Files</u>:</html>"
            }
        }
        tr{
            td(colfill: false, align: "RIGHT") {label "Files"}
            td(colspan: 2, colfill: false, align: "LEFT") {
                textField(id: "inputFiles", columns: 20, toolTipText: "Comma separated paths of input files,"+
        "either relative to maltcms directory or absolute")
                bind(source: inputFiles, sourceProperty: "text", target: props.ifiles,
                    targetProperty: "files",mutual: true)
            }
            td(colfill: true, align: "LEFT") {
                button("...", id: "fileButton", actionPerformed: {
                        def fc = new JFileChooser(dialogTitle:"Select input files",fileSelectionMode: JFileChooser.FILES_AND_DIRECTORIES, multiSelectionEnabled: true,
                            fileFilter:[getDescription: {-> "*.cdf;*.D"}, accept:{file-> file ==~ /.*?\.cdf/ ||
                                    file ==~ /.*?\.CDF/ || file ==~ /.*?\.D/ || file ==~ /.*?\.d/ ||
                                    file.isDirectory() }] as javax.swing.filechooser.FileFilter)
                        fc.currentDirectory = userProps.lastDirectory
                        def retval = fc.showOpenDialog()
                        if(retval == JFileChooser.APPROVE_OPTION) {
							String mode = "ap"
                            if(fc.getSelectedFiles().length>0) {
                                userProps.lastDirectory = fc.getSelectedFiles()[0].getParentFile()
							}
                            List files = fc.getSelectedFiles().collect{
                                file -> 
								if(file.name.toLowerCase().endsWith(".d")) {
									mode = "ap-direct"
								}
								file.path
                            }
                            inputFiles.text = files.join(",")
							setMode(mode, swing, props)
						}
                    })
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(colspan: 3, colfill: true) {
                separator()
            }
        }
        tr {
			td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Pipeline Mode</u>:</html>"
            }
		}
		tr {
			td(colfill: false, align: "RIGHT") {label "Mode"}
			td(colspan: 3, colfill: true, align: "LEFT") {
				textField(id: "pipelineMode", columns: 20, editable: false, toolTipText: "Operating mode of this pipeline, either ap (w/ peakfinding) or ap-direct (w/ imported peaks)")
				bind(source: pipelineMode, sourceProperty: "text", target: props.mr, targetProperty: "pipelineMode",
					mutual:true)
			}
		}
    }
}

def processMonitorTextArea = swing.textArea(id: "processMonitorTextArea", editable: false)

/*
 * Tab for preprocessing settings
 */
def preprocessingTab = swing.panel(constraints: BL.CENTER, id: "preprocessingTab", name: "Preprocessing", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Scan Extractor</u>:</html>"
            }
        }
        tr {
            td(colfill: false, align: "RIGHT") {label "RT Start Time"}
            td(colspan: 3, colfill: true, align: "LEFT") {
                formattedTextField(id: "seStartTime", columns: 20, toolTipText: "Start time from where to extract. -Infinity: start at beginning", formatterFactory: df)
                bind(source: seStartTime, sourceProperty: "value", target: props.se, targetProperty: "startTime",
                    mutual:true)
            }
        }
        tr {
            td(colfill: false, align: "RIGHT") {label "RT Stop Time"}
            td(colspan: 3, colfill: true, align: "LEFT") {
                formattedTextField(id: "seEndTime", columns: 20, toolTipText: "End time to extract up to. Infinity: stop at end of chromatogram", formatterFactory: df)
                bind(source: seEndTime, sourceProperty: "value", target: props.se, targetProperty: "endTime",
                    mutual:true)
            }
        }
        tr {
            td(colspan: 3, colfill: true, align: "RIGHT") {swing.hglue()}
            td(colspan: 1, colfill: false, align: "RIGHT") {
                button("Reset", id: "startStopTimeButton", actionPerformed: {
                        swing.seStartTime.value=Double.NEGATIVE_INFINITY
                        swing.seEndTime.value=Double.POSITIVE_INFINITY
                    })
            }
        }
    }
}

/*
 * Tab for peak detection settings
 */
def peakDetectionTab = swing.panel(constraints: BL.CENTER, id: "peakDetectionTab", name:"Peak Detection", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
		tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Savitzky-Golay Filter</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(colfill: false, align: "RIGHT") {label "Window"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "sgfWindow", columns: 20, toolTipText: "0 < Window <= 12")
                bind(source: sgfWindow, sourceProperty: "value", target: props.sgf, targetProperty: "window",
                    mutual:true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(colspan: 3, colfill: true) {
                separator()
            }
        }
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Loess Baseline Estimator</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Minima Window"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "lbeMinimaWindow", columns: 20, toolTipText: "0 < Minima Window <= ?")
                bind(source: lbeMinimaWindow, sourceProperty: "value", target: props.lbe,
                    targetProperty: "minimaWindow", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Bandwidth"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "lbeBandwidth", columns: 20,formatterFactory: df, toolTipText: "0 < Bandwidth < 1")
                bind(source: lbeBandwidth, sourceProperty: "value", target: props.lbe,
                    targetProperty: "bandwidth", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Robustness Iterations"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "lbeRobustnessIterations", columns: 20, toolTipText: "1 <= Robustness Iterations <= 5")
                bind(source: lbeRobustnessIterations, sourceProperty: "value", target: props.lbe,
                    targetProperty: "robustnessIterations", mutual: true)
            }
        }
		tr {
            td(colfill: true) {hglue()}
            td(colspan: 3, colfill: true) {
                separator()
            }
        }
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Peak Finder</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Peak Threshold"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "tpfPeakThreshold",columns: 20, formatterFactory: df,
                    toolTipText: "0 <= Peak Threshold (db)")
                bind(source: tpfPeakThreshold, sourceProperty: "value", target: props.tpf,
                    targetProperty: "peakThreshold", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Peak Separation Window"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: "tpfPeakSeparationWindow",columns: 20,
                    toolTipText: "0 < Peak Separation Window <= ?")
                bind(source: tpfPeakSeparationWindow, sourceProperty: "value", target: props.tpf,
                    targetProperty: "peakSeparationWindow", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Integrate Raw Signal", visible: false}
            td(colspan: 2, align: "LEFT") {
                checkBox(id: 'tpfIntegrateRawTic', visible: false)
                bind(source: tpfIntegrateRawTic, sourceProperty: "selected", target:props.tpf,
                    targetProperty: "integrateRawTic", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Save Graphics"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                checkBox(id: 'tpfSaveGraphics')
                bind(source: tpfSaveGraphics, sourceProperty: "selected", target: props.tpf,
                    targetProperty: "saveGraphics", mutual: true)
            }
        }
    }
}

def peakNormalizationTab = swing.panel(constraints: BL.CENTER, id: "peakNormalizationTab", name:"Peak Normalization", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Peak Normalizer</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Normalize using"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                comboBox(id: 'tanTicVariableNames', items: props.tan.ticVariableNames)
                bind(source: tanTicVariableNames, sourceEvent:"actionPerformed", sourceValue: {tanTicVariableNames.selectedItem}, target:props.tan, targetProperty: "ticVariableName")
            }
        }
    }
}

/*
 * Tab for peak alignment settings
 */
def peakAlignmentTab = swing.panel(constraints: BL.CENTER, id:"peakAlignmentTab", name:"Peak Alignment", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Peak Matching Similarity</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Retention Time Deviation Tolerance"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'gdsTolerance',columns: 14,formatterFactory: df,
                    toolTipText: "0 < Retention Time Deviation Tolerance <= Maximum Runtime of Chromatograms (s)")
                bind(source: gdsTolerance, sourceProperty: "value", target: props.gds,
                    targetProperty: "tolerance", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Retention Time Deviation Threshold"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'gdsThreshold',columns: 14,formatterFactory: df,
                    toolTipText: "0< Retention Time Deviation Threshold <= 1")
                bind(source: gdsThreshold, sourceProperty: "value", target: props.gds,
                    targetProperty: "threshold", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(colspan: 3, colfill: true) {
                separator()
            }
        }
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label(text: "<html><u>Peak Clique Alignment</u>:</html>")
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Maximum Retention Time Deviation"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'pcaMaxRTDifference',columns: 14,formatterFactory: df,
                    toolTipText: "0< Retention Time Deviation Tolerance < "+
                    "Maximum Retention Time Deviation <= Maximum Runtime of Chromatograms (s)"
                )
                bind(source: pcaMaxRTDifference, sourceProperty: "value", target: props.pca,
                    targetProperty: "maxRTDifference", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Minimum Clique Size"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'pcaMinCliqueSize',columns: 14, toolTipText:
            "-1 or 2 <= Minimum Clique Size <= "+
            "Number of Chromatograms"
                )
                bind(source: pcaMinCliqueSize, sourceProperty: "value", target: props.pca,
                    targetProperty: "minCliqueSize", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Use Peak Area From", visible: false}
            td(colspan: 2, colfill: true, align: "LEFT") {
                textField(id: 'pcaPeakAreaVariable', columns: 14,toolTipText: "peak_area_normalized",
                    visible: false)// or peak_area")
                bind(source: pcaPeakAreaVariable, sourceProperty: "text", target: props.pca,
                    targetProperty: "peakAreaVariable", mutual: true)
            }
        }
    }
}

/*
 * Tab for maltcms runtime settings
 */
def maltcmsRuntimeTab = swing.panel(constraints: BL.CENTER, id: "maltcmsTab", name:"Maltcms", alignmentY: java.awt.Component.TOP_ALIGNMENT, alignmentX: java.awt.Component.LEFT_ALIGNMENT) {
    tableLayout(cellpadding: 5) {
        tr {
            td(colspan: 4, colfill: true, align: "LEFT") {
                label "<html><u>Runtime Arguments</u>:</html>"
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Arguments"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'arguments',columns: 20,
                    toolTipText: "Runtime args, e.g. -Xmx2G to use at most 2 Gigabytes of memory.")
                bind(source: arguments, sourceProperty: "value", target: props.mr,
                    targetProperty: "arguments", mutual: true)
            }
        }
        tr {
            td(colfill: true) {hglue()}
            td(align: "RIGHT") {label "Parallelization"}
            td(colspan: 2, colfill: true, align: "LEFT") {
                formattedTextField(id: 'threads',columns: 20,
                    toolTipText: "Number of parallel threads to use. Should be at most the number of available processors/cores.")
                bind(source: threads, sourceProperty: "value", target: props.mr,
                    targetProperty: "parallelThreads", mutual: true)
            }
        }
    }
}

//def tabbedPane = swing.tabbedPane(id: "tabbedPane") {
//	widget(importTab)
//	widget(preprocessingTab)
//	widget(peakDetectionTab)
//	widget(peakNormalizationTab)
//	widget(peakAlignmentTab)
//	widget(maltcmsRuntimeTab)
//}
//
//def apDirectTabbedPane = swing.tabbedPane(id: "apDirectTabbedPane") {
//	widget(importTab)
//	widget(peakNormalizationTab)
//	widget(peakAlignmentTab)
//	widget(maltcmsRuntimeTab)
//}

/*
 * Content panel
 */
def contentPanel = swing.panel(constraints: BL.CENTER, border: swing.emptyBorder(5),id: "contentPanel") {
    tableLayout(cellpadding: 1) {
        tr {
            td(colspan: 2, colfill: true, rowfill: true, align: "LEFT") {
                tabbedPane(id: "tabbedPane") {
					widget(importTab)
					widget(preprocessingTab)
					widget(peakDetectionTab)
					widget(peakNormalizationTab)
					widget(peakAlignmentTab)
					widget(maltcmsRuntimeTab)
				}
            }
            td(colspan: 1, colfill: true, rowfill: true, rowspan: 10) {
                panel(border: titledBorder("Maltcms Output")) {
                    borderLayout()
                    scrollPane(constraints: BL.CENTER, preferredSize: [400,500]){
                        widget(processMonitorTextArea)
                    }
                }
            }
        }
    }
}

/*
 * Buttons
 */
def buttonPanel = swing.panel(constraints: BL.SOUTH, id: "buttonPanel") {
    button("Load Defaults", actionPerformed: {
			props.load(new File(System.getProperty("ap.home"),"cfg/ap-defaultParameters.properties"))
		})
    separator(orientation: javax.swing.SwingConstants.VERTICAL)
    button("Save", actionPerformed: {
			props.save()
			userProps.save()
		})
    button("Reload", actionPerformed: {
			props.load()
			userProps.load()
		})
    separator(orientation: javax.swing.SwingConstants.VERTICAL)
    button("Start", actionPerformed: {
            props.save()
            userProps.save()
            if(execution!=null) {
                execution.cancel()
                execution = null
            }
            if(execution==null) {
                println "Creating new MaltcmsExecution"
                execution = new MaltcmsExecution(textArea: processMonitorTextArea, inputFiles: props.ifiles.files, arguments: props.mr.arguments, parallelThreads: props.mr.parallelThreads, apProperties: props.mr.pipelineFile)//, //workingDirectory: props.wdir)
            }
            execution.start()
        })
    button("Stop", actionPerformed: {
            if(execution!=null) {
                execution.cancel()
                execution = null
            }
        })
    separator(orientation: javax.swing.SwingConstants.VERTICAL)
    button("Close", actionPerformed: {System.exit(0)})
}

swing.edt {
    frame(title: 'Maltcms Analytical Pyrolysis Settings', show: true,
        pack: true,
        defaultCloseOperation: JFrame.EXIT_ON_CLOSE, id: "mainFrame") {
        borderLayout(vgap: 5)
        widget(contentPanel, constraints: BL.CENTER)
        widget(buttonPanel, constraints: BL.SOUTH)
    }
}

props.load()
userProps.load()
swing.tanTicVariableNames.selectedItem = props.tan.ticVariableName
swing.pipelineMode = props.mr.pipelineMode
setMode(props.mr.pipelineMode, swing, props)
