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

import cross.Factory;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import uk.ac.ebi.jmzml.model.mzml.DataProcessing;
import uk.ac.ebi.jmzml.model.mzml.DataProcessingList;
import uk.ac.ebi.jmzml.model.mzml.FileDescription;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfiguration;
import uk.ac.ebi.jmzml.model.mzml.InstrumentConfigurationList;
import uk.ac.ebi.jmzml.model.mzml.MzML;
import uk.ac.ebi.jmzml.model.mzml.ParamGroup;
import uk.ac.ebi.jmzml.model.mzml.Precursor;
import uk.ac.ebi.jmzml.model.mzml.ProcessingMethod;
import uk.ac.ebi.jmzml.model.mzml.Run;
import uk.ac.ebi.jmzml.model.mzml.Sample;
import uk.ac.ebi.jmzml.model.mzml.Software;
import uk.ac.ebi.jmzml.model.mzml.SoftwareList;
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
		instrumentConfiguration.setSoftwareRef("maltcms");
		icl.getInstrumentConfiguration().add(instrumentConfiguration);
		icl.setCount(0);
		mzML.setInstrumentConfigurationList(icl);
//
//			//add source file
		SourceFile sourceFile = new SourceFile();
		sourceFile.setLocation(inputFileFragment.getUri().toString());
		sourceFile.setName(inputFileName);
		sourceFile.setId(inputFileFragment.getName());
//			<cvParam cvRef="MS" accession="MS:1000567" name="Bruker/Agilent YEP file" value=""/>
//          <cvParam cvRef="MS" accession="MS:1000569" name="SHA-1" value="1234567890123456789012345678901234567890"/>
//			CVParam fileType = new CVParam();
//			fileType.setAccession();
//			fileType.setValue();
//			sourceFile.getCvParam().add();
		String hash = sha1Hash(inputFile);
		if (!hash.isEmpty()) {
			CVParam fileHash = new CVParam();
			fileHash.setAccession("MS:1000569");
			fileHash.setName("SHA-1");
			fileHash.setValue(hash);
			fileHash.setCvRef("MS");
			sourceFile.getCvParam().add(fileHash);
		}
		

		FileDescription fileDescription = new FileDescription();
		ParamGroup fileContent = new ParamGroup();
		fileDescription.setFileContent(fileContent);
		SourceFileList sfl = new SourceFileList();
		sfl.getSourceFile().add(sourceFile);
		sfl.setCount(1);
		fileDescription.setSourceFileList(sfl);
		mzML.setFileDescription(fileDescription);
		
		mzML.setSoftwareList(createSoftwareList());
		DataProcessingList dpl = createDataProcessingList();
		mzML.setDataProcessingList(dpl);
//			//create spectra
		SpectrumList csl = createSpectraList(inputFileName, inputFileFragment, psiMs, dpl.getDataProcessing().get(0));
//			//create chromatogram
		ChromatogramList cl = createChromatogramList(inputFileFragment, psiMs, dpl.getDataProcessing().get(0));
//			//mzML.setAccession(accession);
//			//mzML.setCvList(null);
//			//mzML.setFileDescription(null);
//			//mzML.setId(accession);
//			//mzML.setInstrumentConfigurationList(null);
//			//mzML.setReferenceableParamGroupList(null);
//			//mzML.setSampleList(null);
//			//mzML.setScanSettingsList(null);
		mzML.setVersion("1.1.0");
		RunBuilder runBuilder = new RunBuilder();
		runBuilder.id("maltcms-" + inputFileName + "-run").defaultInstrumentConfiguration(instrumentConfiguration).defaultSourceFiles(sourceFile).spectrumList(csl).chromatogramList(cl);
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

	protected DataProcessingList createDataProcessingList() {
		DataProcessingList list = new DataProcessingList();
		List<DataProcessing> dpList = list.getDataProcessing();
		DataProcessing dp = new DataProcessing();
		dp.setId("maltcms_processing");
		List<ProcessingMethod> pmList = dp.getProcessingMethod();
		ProcessingMethod pm = new ProcessingMethod();
		pm.setOrder(1);
		pm.setSoftwareRef("maltcms");
		List<CVParam> cvParams = pm.getCvParam();
		CVParam cvTerm = new CVParam();
		//file format conversion
		cvTerm.setAccession("MS:1000530");
		cvTerm.setName("file format conversion");
		cvTerm.setCvRef("MS");
		cvParams.add(cvTerm);
		CVParam cvTerm2 = new CVParam();
		//Conversion to mzML
		cvTerm2.setAccession("MS:1000544");
		cvTerm2.setName("Conversion to mzML");
		cvTerm2.setCvRef("MS");
		cvParams.add(cvTerm2);
		pmList.add(pm);
		dpList.add(dp);
		list.setCount(dpList.size());
		return list;
	}

	protected String sha1Hash(URI file) {
		File f = new File(file);
		if (f.isFile() && f.exists()) {
			MessageDigest sha1 = null;
			InputStream is = null;
			try {
				sha1 = MessageDigest.getInstance("SHA-1");
				is = file.toURL().openStream();
				byte[] dataBytes = new byte[4096];
				int nread = 0;
				while ((nread = is.read(dataBytes)) != -1) {
					sha1.update(dataBytes, 0, nread);
				}
				byte[] digestBytes = sha1.digest();
				String result;
				try (Formatter formatter = new Formatter()) {
					for (byte b : digestBytes) {
						formatter.format("%02x", b);
					}
					result = formatter.toString();
				}
				return result;
			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(MZMLExporterWorker.class.getName()).log(Level.SEVERE, null, ex);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(MZMLExporterWorker.class.getName()).log(Level.SEVERE, null, ex);
			} catch (MalformedURLException ex) {
				Logger.getLogger(MZMLExporterWorker.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(MZMLExporterWorker.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
					}
				}
			}

		} else {
			log.warn("URI resource is not a local file, skipping checksum generation! {}", file);
		}
		return "";
	}

	protected SoftwareList createSoftwareList() {
		SoftwareList list = new SoftwareList();
		List<Software> softwareList = list.getSoftware();
		Software software = new Software();
		software.setId("maltcms");
		software.setVersion(Factory.getInstance().getConfiguration().getString("application.version", "undefined"));
		List<CVParam> cvparams = software.getCvParam();
		CVParam cvTerm = new CVParam();
		cvTerm.setAccession("MS:1002344");
		cvTerm.setName("Maltcms");
		cvTerm.setCvRef("MS");
		cvparams.add(cvTerm);
		softwareList.add(software);
		list.setCount(softwareList.size());
		return list;
	}

	protected ChromatogramList createChromatogramList(FileFragment inputFileFragment, CV psiMs, DataProcessing dataProcessing) throws ResourceNotAvailableException {
		IVariableFragment ticValues = inputFileFragment.getChild(totalIntensityVariable);
		ChromatogramList cl = new ChromatogramList();
		cl.setDefaultDataProcessing(dataProcessing);
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
		c.setDefaultArrayLength(ticValues.getRange()[0].length());
		Object ticDataArray = ticValues.getArray().get1DJavaArray(ticDataType.getPrimitiveClassType());
		int cbdaEncodedLength = 0;
		switch (ticDataType) {
			case DOUBLE:
				cbdaEncodedLength = cbda.set64BitFloatArrayAsBinaryData((double[]) ticDataArray, true, psiMs);
				break;
			case FLOAT:
				cbdaEncodedLength = cbda.set32BitFloatArrayAsBinaryData((float[]) ticDataArray, true, psiMs);
				break;
			case INT:
				cbdaEncodedLength = cbda.set32BitIntArrayAsBinaryData((int[]) ticDataArray, true, psiMs);
				break;
			case LONG:
				cbdaEncodedLength = cbda.set64BitIntArrayAsBinaryData((long[]) ticDataArray, true, psiMs);
				break;
		}
		cbda.setEncodedLength(cbdaEncodedLength);
		cbda.setDataProcessing(dataProcessing);
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
		int satbdaEncodedLength = 0;
		switch (satDataType) {
			case DOUBLE:
				satbdaEncodedLength = satbda.set64BitFloatArrayAsBinaryData((double[]) satDataArray, true, psiMs);
				break;
			case FLOAT:
				satbdaEncodedLength = satbda.set32BitFloatArrayAsBinaryData((float[]) satDataArray, true, psiMs);
				break;
			case INT:
				satbdaEncodedLength = satbda.set32BitIntArrayAsBinaryData((int[]) satDataArray, true, psiMs);
				break;
			case LONG:
				satbdaEncodedLength = satbda.set64BitIntArrayAsBinaryData((long[]) satDataArray, true, psiMs);
				break;
		}
		satbda.setEncodedLength(satbdaEncodedLength);
		satbda.setDataProcessing(dataProcessing);
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
		cbdal.setCount(l.size());
		c.setBinaryDataArrayList(cbdal);
		cl.getChromatogram().add(c);
		cl.setCount(1);
		return cl;
	}

	protected SpectrumList createSpectraList(String inputFileName, FileFragment inputFileFragment, CV psiMs, DataProcessing dataProcessing) throws ResourceNotAvailableException {
		//prepare spectra
		SpectrumList sl = new SpectrumList();
		sl.setDefaultDataProcessing(dataProcessing);
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
		System.out.println("Mass data type: " + massDataType.getPrimitiveClassType());
		DataType intensityDataType = intensityValues.getDataType();
		System.out.println("Intensity values data type: " + intensityDataType.getPrimitiveClassType());
		for (int i = 0; i < scans; i++) {
			Spectrum s = new Spectrum();
			s.setIndex(i);
			s.setId(i + "");
			BinaryDataArrayList bdal = new BinaryDataArrayList();
			List<BinaryDataArray> bdas = bdal.getBinaryDataArray();
			BinaryDataArray masses = new BinaryDataArray();
			Object massDataArray = indexedMassValues.get(i).get1DJavaArray(massDataType.getPrimitiveClassType());
			int massesEncodedLength = 0;
			switch (massDataType) {
				case DOUBLE:
					massesEncodedLength = masses.set64BitFloatArrayAsBinaryData((double[]) massDataArray, true, psiMs);
					break;
				case FLOAT:
					massesEncodedLength = masses.set32BitFloatArrayAsBinaryData((float[]) massDataArray, true, psiMs);
					break;
				case INT:
					massesEncodedLength = masses.set32BitIntArrayAsBinaryData((int[]) massDataArray, true, psiMs);
					break;
				case LONG:
					massesEncodedLength = masses.set64BitIntArrayAsBinaryData((long[]) massDataArray, true, psiMs);
					break;
			}
			masses.setEncodedLength(massesEncodedLength);
			bdas.add(masses);
			BinaryDataArray intensities = new BinaryDataArray();
			Object intensityDataArray = indexedIntensityValues.get(i).get1DJavaArray(intensityDataType.getPrimitiveClassType());
			int intensitiesEncodedLength = 0;
			switch (intensityDataType) {
				case DOUBLE:
					intensitiesEncodedLength = intensities.set64BitFloatArrayAsBinaryData((double[]) intensityDataArray, true, psiMs);
					break;
				case FLOAT:
					intensitiesEncodedLength = intensities.set32BitFloatArrayAsBinaryData((float[]) intensityDataArray, true, psiMs);
					break;
				case INT:
					intensitiesEncodedLength = intensities.set32BitIntArrayAsBinaryData((int[]) intensityDataArray, true, psiMs);
					break;
				case LONG:
					intensitiesEncodedLength = intensities.set64BitIntArrayAsBinaryData((long[]) intensityDataArray, true, psiMs);
					break;
			}
			intensities.setEncodedLength(intensitiesEncodedLength);
			bdas.add(intensities);
			bdal.setCount(bdas.size());
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
			if (sr != null) {
				r.setSampleRef(sr.getId());
			}
			r.setSpectrumList(sl);
			if (sts != null) {
				r.setStartTimeStamp(sts);
			}
			return r;
		}
	}
}
