/*
 * Copyright (C) 2008, 2009 Nils Hoffmann Nils.Hoffmann A T
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
 * 
 * $Id$
 */

package maltcms.tests;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;
import apps.Maltcms;
import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;

/**
 * Test FileFragment and VariableFragment.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class FragmentTest extends TestCase {

	protected static final String fileName = "chromatogram_25029_129.70.160.104_1188470505_DM1795_20060912.xml";

	protected static IFileFragment ff = null;

	protected static Maltcms m = null;

	static {
		FragmentTest.m = Maltcms.getInstance();
		Factory.getInstance().configure(FragmentTest.m.getDefaultConfig());
		Factory.getInstance().getConfiguration().setProperty("input.basedir",
		        "/vol/maltcms/data/mzXML");
		Factory.getInstance().getConfiguration().setProperty("output.basedir",
		        "/vol/maltcms/output/fragmentTest");
		final File f = new File("/vol/maltcms/data/mzXML/",
		        FragmentTest.fileName);
		FragmentTest.ff = Factory.getInstance().getFileFragmentFactory()
		        .create(f);
	}

	public void testCreate() {
		// Maltcms m = Maltcms.getInstance();
		// ArrayFactory.configure(m.getDefaultConfig());
		// ArrayFactory.getConfiguration().setProperty("input.basedir",
		// "/vol/maltcms/data/RI");
		// ArrayFactory.getConfiguration().setProperty("output.basedir",
		// "/vol/maltcms/output/fragmentTest");
		// VariableFragment di1, di2, di3, di4;
		// FileFragment ff1 = FragmentTools
		// .fromString(
		// "Std_Mix_RI_03.cdf>total_intensity&mass_values#scan_index");
		// System.out.println(ff1.toString());
		// if(!ff1.hasChild("total_intensity")) {
		// throw new IllegalArgumentException();
		// }
		// di1 = ff1.getChild("total_intensity");
		// di3 = ff1.getChild("mass_values");
		// FileFragment ff2 = FragmentTools
		// .fromString(
		// "Std_Mix_RI_02.cdf>total_intensity&mass_values#scan_index");
		// di2 = ff2.getChild("total_intensity");
		// di4 = ff2.getChild("mass_values");
		// ArrayList<String> al = new ArrayList<String>();
		//		
		// al.add(di1.getVarname());
		// al.add(di2.getVarname());
		// // NormalizationFilter nf = new NormalizationFilter();
		//	
		// Array a1 = FragmentTools.getArrayFor(di1);
		// Array a2 = FragmentTools.getArrayFor(di2);
		// ArrayList<Array> a3 = FragmentTools.getArraysForIndexed(di3);
		// ArrayList<Array> a4 = FragmentTools.getArraysForIndexed(di4);
		// //FragmentTools.setArrayForFragment(di1, a1);
		// //FragmentTools.setArrayForFragment(di2, a2);
		// Assert.assertTrue(FragmentTools.saveToFile(di1.getParent()));
		// Assert.assertTrue(FragmentTools.saveToFile(di2.getParent()));
		// Element root1 = new Element("maltcms");
		// Document d1 = new Document(root1);
		// //Element ff = new Element("file");
		// di1.getParent().appendXML(root1);
		// Element root2 = new Element("maltcms");
		// Document d2 = new Document(root2);
		// //Element ff = new Element("file");
		// di2.getParent().appendXML(root2);
		//		
		// XMLOutputter outp = new XMLOutputter();
		// try {
		// outp.output(d1, System.out);
		// }
		// catch (IOException e) {
		// System.err.println(e);
		// }
		// try {
		// outp.output(d2, System.out);
		// } catch (IOException e) {
		// System.err.println(e);
		// }
	}

	public void testFragmentDeSerializer() {
		throw new NotImplementedException();
		// FileFragment ff =
		// FragmentTools.getFragment("/vol/maltcms/data/mzXML", fileName);
		// final FragmentXMLSerializer fs = new FragmentXMLSerializer();
		// fs.init(FragmentTest.ff);
		// final IFileFragment f1 = fs.serialize();
		// System.err.println(f1);
		// // ff =
		// // FragmentTools.getFragment("/vol/maltcms/output/fragmentTest",
		// // "maltcms_fragment_structure.xml");
		// final IFileFragment f2 = fs.deserialize();
		// final int i = f1.compareTo(f2);
		// System.err.println(f2);
		// System.err.println(i);
	}

	public void testMZXML() {
		// FileFragment ff =
		// FragmentTools.fromString("/vol/maltcms/data/mzXML"+ArrayFactory.
		// getConfiguration
		// ().getString("file.separator")+fileName+">total_intensity");
		// VariableFragment total_intensities = FragmentTools.getVariable(ff,
		// "total_intensity");
		// Array arr = total_intensities.getArray();
		for (final IVariableFragment vf : FragmentTest.ff) {
			Assert.assertNotNull(vf);
			Assert.assertNotNull(vf.getArray());
			System.out.println(vf);
		}
		System.err.println(FragmentTest.ff);
		FragmentTest.ff.save();
		System.err.println(FragmentTest.ff);
		Factory.getInstance().getConfiguration().setProperty("input.basedir",
		        "/vol/maltcms/output/fragmentTest");
		// ff.setFilename("maltcms_fragment_structure.xml");

		// try {
		// VariableFragment scan_500_masses = FragmentTools.getVariable(ff,
		// "mass_values",null,null,null,new Range(500,501,1));
		// VariableFragment scan_500_intensities = FragmentTools.getVariable(ff,
		// "intensity_values",null,null,null,new Range(500,501,1));
		// Array scan_500_m = FragmentTools.getArrayFor(scan_500_masses);
		// Array scan_500_i = FragmentTools.getArrayFor(scan_500_intensities);
		// System.out.println(scan_500_m);
		// System.out.println(scan_500_i);
		// } catch (InvalidRangeException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
