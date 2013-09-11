/*
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.commands.fragments.io;

import cross.annotations.Configurable;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tools.EvalTools;
import cross.datastructures.tools.FileTools;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import maltcms.commands.fragments.io.mzml.CachedSpectrumList;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import static ucar.ma2.DataType.DOUBLE;
import static ucar.ma2.DataType.FLOAT;
import static ucar.ma2.DataType.INT;
import static ucar.ma2.DataType.LONG;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArray;
import uk.ac.ebi.jmzml.model.mzml.BinaryDataArrayList;
import uk.ac.ebi.jmzml.model.mzml.CV;
import uk.ac.ebi.jmzml.model.mzml.CVList;
import uk.ac.ebi.jmzml.model.mzml.CVParam;
import uk.ac.ebi.jmzml.model.mzml.Chromatogram;
import uk.ac.ebi.jmzml.model.mzml.ChromatogramList;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfigurationList;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.Sample;
import uk.ac.ebi.jmzml.model.mzml.SourceFile;
import uk.ac.ebi.jmzml.model.mzml.SourceFileList;
import uk.ac.ebi.jmzml.model.mzml.Spectrum;
import uk.ac.ebi.jmzml.model.mzml.SpectrumList;
import uk.ac.ebi.jmzml.xml.io.MzMLMarshaller;

/**
 *
 * @author Nils Hoffmann
 */
@Slf4j
@Data
public class MZMLExporterWorker implements Callable<URI>, Serializable {

	@Configurable(description = "The input file URI to read from. May be remote.")
	private URI inputFile;
	private String inputFileId;
	@Configurable(description = "The output file URI to write to. Must be local.")
	private URI outputFile;
	@Configurable(description = "The psi ms controlled vocabulary version to use.")
	private String psiMsVersion = "3.48.0";
	@Configurable(description = "The unit ontology controlled vocabulary version to use.")
	private String unitOntologyVersion = "12:10:2011";
	@Configurable(description = "Whether spectral data should be gzip compressed or not.")
	private boolean compressSpectra = true;
	private String scanIndexVariable;
	private String massValuesVariable;
	private String intensityValuesVariable;
	private String totalIntensityVariable;
	private String scanAcquisitionTimeVariable;

	@Override
	public URI call() throws Exception {
//		try {
			log.info("Creating mzML file for {}", inputFile);
			String inputFileName = StringTools.removeFileExt(FileTools.getFilename(inputFile));
			FileFragment inputFileFragment = new FileFragment(inputFile);
			FileFragment outputFileFragment = new FileFragment(outputFile);
			//add input file as source file
			outputFileFragment.addSourceFile(inputFileFragment);
			MzML mzML = new MzML();
			CVList cvlist = new CVList();
			//setup psi ms controlled vocabulary
			CV psiMs = new CV();
			psiMs.setId("MS");
			psiMs.setFullName("Proteomics Standards Initiative Mass Spectrometry Ontology");
			psiMs.setVersion(psiMsVersion);
			psiMs.setURI("http://psidev.cvs.sourceforge.net/*checkout*/psidev/psi/psi-ms/mzML/controlledVocabulary/psi-ms.obo");
			//setup unit ontology controlled vocabulary
			CV unitOntology = new CV();
			unitOntology.setId("UO");
			unitOntology.setFullName("Unit Ontology");
			unitOntology.setVersion(unitOntologyVersion);
			unitOntology.setURI("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo");
			cvlist.getCv().add(psiMs);
			cvlist.getCv().add(unitOntology);
			cvlist.setCount(2);
			mzML.setCvList(cvlist);

			InstrumentConfigurationList icl = new InstrumentConfigurationList();
			InstrumentConfiguration instrumentConfiguration = new InstrumentConfiguration();
			instrumentConfiguration.setId("instrument-configuration");
			icl.getInstrumentConfiguration().add(instrumentConfiguration);
			icl.setCount(1);
			mzML.setInstrumentConfigurationList(icl);
//
//			//add source file
			SourceFile sourceFile = new SourceFile();
			sourceFile.setLocation(inputFileFragment.getUri().toString());
			sourceFile.setName(inputFileName);
			sourceFile.setId(inputFileId);
			
			FileDescription fileDescription = new FileDescription();
			SourceFileList sfl = new SourceFileList();
			sfl.getSourceFile().add(sourceFile);
			sfl.setCount(1);
			fileDescription.setSourceFileList(sfl);
			mzML.setFileDescription(fileDescription);
//			//create spectra
			SpectrumList csl = createSpectraList(inputFileName, inputFileFragment, psiMs);
//			//create chromatogram
			ChromatogramList cl = createChromatogramList(inputFileFragment, psiMs);
//			//mzML.setAccession(accession);
//			//mzML.setCvList(null);
//			//mzML.setDataProcessingList(null);
//			//mzML.setFileDescription(null);
//			//mzML.setId(accession);
//			//mzML.setInstrumentConfigurationList(null);
//			//mzML.setReferenceableParamGroupList(null);
//			//mzML.setSampleList(null);
//			//mzML.setScanSettingsList(null);
//			//mzML.setSoftwareList(null);
//			//mzML.setVersion();
			RunBuilder runBuilder = new RunBuilder();
			runBuilder.id("maltcms-"+inputFileName+"-run").defaultInstrumentConfiguration(instrumentConfiguration).defaultSourceFiles(sourceFile).spectrumList(csl).chromatogramList(cl);
			mzML.setRun(runBuilder.build());
			MzMLMarshaller mzmlMarshaller = new MzMLMarshaller();
			String basename = StringTools.removeFileExt(outputFileFragment.getName());
			File f = new File(new File(outputFileFragment.getUri()).getParentFile(), basename + ".mzml");
			log.info("Storing mzML file {}", f);
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f))) {
				mzmlMarshaller.marshall(mzML, bos);
				bos.close();
			}
			//add the mzml file as a source file of the returned fragment for later reuse and variable retrieval
			outputFileFragment.addSourceFile(new FileFragment(f));
			//persist to file
			log.info("Saving output file fragment!");
			outputFileFragment.save();
			return outputFileFragment.getUri();
//		} catch (Exception e) {
//			log.error("Caught exception while exporting to mzML:", e);
//			throw e;
//		}
	}

	protected ChromatogramList createChromatogramList(FileFragment inputFileFragment, CV psiMs) throws ResourceNotAvailableException {
		IVariableFragment ticValues = inputFileFragment.getChild(totalIntensityVariable);
		ChromatogramList cl = new ChromatogramList();
		Chromatogram c = new Chromatogram();
		c.setIndex(0);
		c.setId("tic");
		CVParam ticParam = new CVParam();
		ticParam.setCvRef("MS");
		ticParam.setAccession("MS:1000235");
		ticParam.setName("total ion current chromatogram");
		c.getCvParam().add(ticParam);
		BinaryDataArrayList cbdal = new BinaryDataArrayList();
		List<BinaryDataArray> l = cbdal.getBinaryDataArray();
		DataType ticDataType = ticValues.getDataType();
		BinaryDataArray cbda = new BinaryDataArray();
		Object ticDataArray = ticValues.getArray().get1DJavaArray(ticDataType.getPrimitiveClassType());
		switch (ticDataType) {
			case DOUBLE:
				cbda.set64BitFloatArrayAsBinaryData((double[]) ticDataArray, true, psiMs);
				break;
			case FLOAT:
				cbda.set32BitFloatArrayAsBinaryData((float[]) ticDataArray, true, psiMs);
				break;
			case INT:
				cbda.set32BitIntArrayAsBinaryData((int[]) ticDataArray, true, psiMs);
				break;
			case LONG:
				cbda.set64BitIntArrayAsBinaryData((long[]) ticDataArray, true, psiMs);
				break;
		}
		CVParam ticCvParam = new CVParam();
		ticCvParam.setCvRef("MS");
		ticCvParam.setAccession("MS:1000515");
		ticCvParam.setName("intensity array");
		ticCvParam.setUnitCvRef("MS");
		ticCvParam.setUnitAccession("MS:1000131");
		ticCvParam.setUnitName("number of counts");
		cbda.getCvParam().add(ticCvParam);
		l.add(cbda);
		IVariableFragment satValues = inputFileFragment.getChild(scanAcquisitionTimeVariable);
		DataType satDataType = satValues.getDataType();
		BinaryDataArray satbda = new BinaryDataArray();
		Object satDataArray = satValues.getArray().get1DJavaArray(satDataType.getPrimitiveClassType());
		switch (ticDataType) {
			case DOUBLE:
				satbda.set64BitFloatArrayAsBinaryData((double[]) satDataArray, true, psiMs);
				break;
			case FLOAT:
				satbda.set32BitFloatArrayAsBinaryData((float[]) satDataArray, true, psiMs);
				break;
			case INT:
				satbda.set32BitIntArrayAsBinaryData((int[]) satDataArray, true, psiMs);
				break;
			case LONG:
				satbda.set64BitIntArrayAsBinaryData((long[]) satDataArray, true, psiMs);
				break;
		}
		CVParam satCvParam = new CVParam();
		satCvParam.setCvRef("MS");
		satCvParam.setAccession("MS:1000595");
		satCvParam.setName("time array");
		satCvParam.setUnitCvRef("UO");
		satCvParam.setUnitAccession("UO:0000010");
		//TODO extract correct unit from original attributes and correct accession number
		satCvParam.setUnitName("second");
		satbda.getCvParam().add(satCvParam);
		l.add(satbda);
		cbdal.setCount(1);
		cl.getChromatogram().add(c);
		cl.setCount(1);
		return cl;
	}

	protected SpectrumList createSpectraList(String inputFileName, FileFragment inputFileFragment, CV psiMs) throws ResourceNotAvailableException {
		//prepare spectra
		SpectrumList sl = new SpectrumList();
		List<Spectrum> csl = sl.getSpectrum();
//		CachedSpectrumList csl = new CachedSpectrumList(inputFileName);
		IVariableFragment scanIndex = inputFileFragment.getChild(scanIndexVariable);
		IVariableFragment massValues = inputFileFragment.getChild(massValuesVariable);
		massValues.setIndex(scanIndex);
		IVariableFragment intensityValues = inputFileFragment.getChild(intensityValuesVariable);
		intensityValues.setIndex(scanIndex);
		int scans = scanIndex.getDimensions()[0].getLength();
		List<Array> indexedMassValues = massValues.getIndexedArray();
		List<Array> indexedIntensityValues = intensityValues.getIndexedArray();
		DataType massDataType = massValues.getDataType();
		System.out.println("Mass data type: "+massDataType.getPrimitiveClassType());
		DataType intensityDataType = intensityValues.getDataType();
		System.out.println("Intensity values data type: "+intensityDataType.getPrimitiveClassType());
		for (int i = 0; i < scans; i++) {
			Spectrum s = new Spectrum();
			s.setIndex(i);
			s.setId(i + "");
			BinaryDataArrayList bdal = new BinaryDataArrayList();
			List<BinaryDataArray> bdas = bdal.getBinaryDataArray();
			BinaryDataArray masses = new BinaryDataArray();
			Object massDataArray = indexedMassValues.get(i).get1DJavaArray(massDataType.getPrimitiveClassType());
			switch (massDataType) {
				case DOUBLE:
					masses.set64BitFloatArrayAsBinaryData((double[]) massDataArray, true, psiMs);
					break;
				case FLOAT:
					masses.set32BitFloatArrayAsBinaryData((float[]) massDataArray, true, psiMs);
					break;
				case INT:
					masses.set32BitIntArrayAsBinaryData((int[]) massDataArray, true, psiMs);
					break;
				case LONG:
					masses.set64BitIntArrayAsBinaryData((long[]) massDataArray, true, psiMs);
					break;
			}
			bdas.add(masses);
			BinaryDataArray intensities = new BinaryDataArray();
			Object intensityDataArray = indexedIntensityValues.get(i).get1DJavaArray(intensityDataType.getPrimitiveClassType());
			switch (intensityDataType) {
				case DOUBLE:
					intensities.set64BitFloatArrayAsBinaryData((double[]) intensityDataArray, true, psiMs);
					break;
				case FLOAT:
					intensities.set32BitFloatArrayAsBinaryData((float[]) intensityDataArray, true, psiMs);
					break;
				case INT:
					intensities.set32BitIntArrayAsBinaryData((int[]) intensityDataArray, true, psiMs);
					break;
				case LONG:
					intensities.set64BitIntArrayAsBinaryData((long[]) intensityDataArray, true, psiMs);
					break;
			}
			bdas.add(intensities);
			s.setBinaryDataArrayList(bdal);
			s.setId("scan=" + i);
			csl.add(s);
		}
		sl.setCount(csl.size());
		return sl;
	}

	public class RunBuilder {

		ChromatogramList cl;
		InstrumentConfiguration ic;
		SourceFile sf;
		String id;
		Sample sr;
		SpectrumList sl;
		Calendar sts;

		public RunBuilder chromatogramList(ChromatogramList cl) {
			this.cl = cl;
			return this;
		}

		public RunBuilder defaultInstrumentConfiguration(InstrumentConfiguration ic) {
			EvalTools.notNull(id, "Run default instrument configuration can not be null!", this);
			this.ic = ic;
			return this;
		}

		public RunBuilder defaultSourceFiles(SourceFile sf) {
			this.sf = sf;
			return this;
		}

		public RunBuilder id(String id) {
			EvalTools.notNull(id, "Run id can not be null!", this);
			this.id = id;
			return this;
		}

		public RunBuilder sample(Sample sr) {
			this.sr = sr;
			return this;
		}

		public RunBuilder spectrumList(SpectrumList sl) {
			this.sl = sl;
			return this;
		}

		public RunBuilder startTimeStamp(Calendar sts) {
			this.sts = sts;
			return this;
		}

		Run build() throws ConstraintViolationException {
			EvalTools.notNull(id, "Run default instrument configuration can not be null!", this);
			EvalTools.notNull(id, "Run id can not be null!", this);
			Run r = new Run();
			r.setChromatogramList(cl);
			r.setDefaultInstrumentConfigurationRef(ic.getId());
			r.setDefaultSourceFileRef(sf.getId());
			r.setId(id);
			if(sr!=null) {
				r.setSampleRef(sr.getId());
			}
			r.setSpectrumList(sl);
			if(sts!=null) {
				r.setStartTimeStamp(sts);
			}
			return r;
		}
	}
}
