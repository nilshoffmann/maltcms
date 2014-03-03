/*
 * Maltcms, modular application toolkit for chromatography mass-spectrometry.
 * Copyright (C) 2008-2014, The authors of Maltcms. All rights reserved.
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
package net.sf.maltcms.ap

import groovy.transform.Canonical
import groovy.beans.Bindable

/**
 *
 * @author Nils Hoffmann
 */
@Canonical
class APProperties {

    File wdir = new File(System.getProperty("ap.home"))
    InputFiles ifiles = new InputFiles()
    ScanExtractor se = new ScanExtractor()
    SavitzkyGolayFilter sgf = new SavitzkyGolayFilter()
    LoessBaselineEstimator lbe = new LoessBaselineEstimator()
    TicPeakFinder tpf = new TicPeakFinder()
    TicAreaNormalizer tan = new TicAreaNormalizer()
    GaussianDifferenceSimilarity gds = new GaussianDifferenceSimilarity()
    PeakCliqueAlignment pca = new PeakCliqueAlignment()
    MaltcmsRuntime mr = new MaltcmsRuntime()
    Utils u = new Utils();

    public void load() {
        File customProperties = new File(System.getProperty("user.dir"),"ap-parameters.properties")
        if (customProperties.exists()) {
            load(customProperties)
        } else {
            load(new File(System.getProperty("ap.home"),"cfg/pipelines/ap-defaultParameters.properties"))
        }
    }

    public void load(File f) {
        def props = new Properties()
        f.withInputStream {
            stream -> props.load(stream)
        }
        ifiles.files = props["inputFiles"]
        wdir = new File(props["workingDirectory"])
        if(!wdir.exists()) {
            //reset in case of leftovers from old layout
            wdir = new File(System.getProperty("ap.home"))
        }
        se.startTime = u.convDouble(props["scanExtractor.startTime"],Double.NEGATIVE_INFINITY)
        se.endTime = u.convDouble(props["scanExtractor.endTime"],Double.POSITIVE_INFINITY)
        sgf.window = u.convInteger(props["savitzkyGolayFilter.window"],12)
        lbe.minimaWindow = u.convInteger(props["loessBaselineEstimator.minimaWindow"],1000)
        lbe.robustnessIterations = u.convInteger(props["loessBaselineEstimator.robustnessIterations"],2)
        lbe.bandwidth = u.convDouble(props["loessBaselineEstimator.bandwidth"],0.3)
        tpf.peakThreshold = u.convDouble(props["ticPeakFinder.peakThreshold"],3.0)
        tpf.peakSeparationWindow = u.convInteger(props["ticPeakFinder.peakSeparationWindow"],10)
        tpf.integrateRawTic =  u.convBoolean(props["ticPeakFinder.integrateRawTic"],true)
        tpf.saveGraphics =  u.convBoolean(props["ticPeakFinder.saveGraphics"],true)
        tan.ticVariableName = u.convString(props["ticAreaNormalizer.ticVariableName"],"total_intensity")
        gds.tolerance =  u.convDouble(props["gaussianDifferenceSimilarity.tolerance"],0.05)
        gds.threshold =  u.convDouble(props["gaussianDifferenceSimilarity.threshold"],0.0)
        pca.maxRTDifference =  u.convDouble(props["peakCliqueAlignment.maxRTDifference"],60.0)
        pca.minCliqueSize =  u.convInteger(props["peakCliqueAlignment.minCliqueSize"],-1)
        pca.peakAreaVariable = u.convString(props["peakCliqueAlignment.peakAreaVariable"],"peak_area_normalized")
        pca.rtNormalizationFactor = u.convDouble(props["peakCliqueAlignment.rtNormalizationFactor"],1.0)
        pca.rtOutputFormat = u.convString(props["peakCliqueAlignment.rtOutputFormat"],"0.00000000000000")
        mr.arguments = u.convString(props["maltcmsRuntime.arguments"],"-Xmx1G")
        mr.parallelThreads = u.convInteger(props["maltcmsRuntime.parallelThreads"],1)
        mr.pipelineMode = u.convString(props["maltcmsRuntime.pipelineMode"],"ap")
        mr.pipelineFile = new File(u.convString(props["maltcmsRuntime.pipelineFile"],new File(System.getProperty("ap.home"),"pipelines/${mr.pipelineMode}.mpl").absolutePath))
        mr.uniqueOutputDir = u.convBoolean(props["maltcmsRuntime.uniqueOutputDir"],true)
        mr.lastOutputDir  = u.convString(props["maltcmsRuntime.lastOutputDir"],"")
    }

    public void save() {
        File f = new File(System.getProperty("user.dir"),"ap-parameters.properties")
        def props = new Properties()
        props["workingDirectory"] = wdir.absolutePath
        props["inputFiles"] = ifiles.files
        props["scanExtractor.startTime"] = se.startTime.toString()
        props["scanExtractor.endTime"] = se.endTime.toString()
        props["savitzkyGolayFilter.window"] = sgf.window.toString()
        props["loessBaselineEstimator.minimaWindow"] = lbe.minimaWindow.toString()
        props["loessBaselineEstimator.robustnessIterations"] = lbe.robustnessIterations.toString()
        props["loessBaselineEstimator.bandwidth"] = lbe.bandwidth.toString()
        props["ticPeakFinder.peakThreshold"] = tpf.peakThreshold.toString()
        props["ticPeakFinder.peakSeparationWindow"] = tpf.peakSeparationWindow.toString()
        props["ticPeakFinder.integrateRawTic"] = tpf.integrateRawTic.toString()
        props["ticPeakFinder.saveGraphics"] = tpf.saveGraphics.toString()
        props["ticAreaNormalizer.ticVariableName"] = tan.ticVariableName.toString()
        props["gaussianDifferenceSimilarity.tolerance"] = gds.tolerance.toString()
        props["gaussianDifferenceSimilarity.threshold"] = gds.threshold.toString()
        props["peakCliqueAlignment.maxRTDifference"] = pca.maxRTDifference.toString()
        props["peakCliqueAlignment.minCliqueSize"] = pca.minCliqueSize.toString()
        props["peakCliqueAlignment.peakAreaVariable"] = pca.peakAreaVariable.toString()
        props["peakCliqueAlignment.rtNormalizationFactor"] = pca.rtNormalizationFactor.toString()
        props["peakCliqueAlignment.rtOutputFormat"] = pca.rtOutputFormat.toString()
        props["maltcmsRuntime.arguments"] = mr.arguments.toString()
        props["maltcmsRuntime.parallelThreads"] = mr.parallelThreads.toString()
        props["maltcmsRuntime.pipelineMode"] = mr.pipelineMode.toString()
        props["maltcmsRuntime.pipelineFile"] = mr.pipelineFile.absolutePath
        props["maltcmsRuntime.uniqueOutputDir"] = mr.uniqueOutputDir.toString()
        props["maltcmsRuntime.lastOutputDir"] = mr.lastOutputDir
        f.withOutputStream {
            stream -> props.store(stream, "maltcms-ap parameters")
        }
    }
}

