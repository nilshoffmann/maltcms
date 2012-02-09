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
package net.sf.maltcms.evaluation.spi.caap;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.nc2.Dimension;
import cross.Factory;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.VariableFragment;
import cross.tools.StringTools;
import maltcms.io.xml.bindings.openms.featurexml.FeatureMap;
import maltcms.io.xml.bindings.openms.featurexml.FeatureType;
import maltcms.io.xml.bindings.openms.featurexml.FeatureType.Position;

/**
 *
 * @author nilshoffmann
 */
public class PeakFactory {

    public static FeatureMap.FeatureList getFeatureList(File f) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("maltcms.io.xml.openms.featureXml");
            final Unmarshaller u = jc.createUnmarshaller();
            final FeatureMap mzd = (FeatureMap) u.unmarshal(f);
            return mzd.getFeatureList();
        } catch (final JAXBException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public static IFileFragment joinFeatures(File outputDir, String filename, FeatureMap.FeatureList fl) {
        TreeMap<Double,TreeMap<Double,Double>> featureMap = new TreeMap<Double,TreeMap<Double,Double>>();
        //first dim is rt
        //second dim is mz
        //intensity
        int features = 0;
        for(FeatureType ft:fl.getFeature()) {
            List<Position> l = ft.getPosition();
            double rt = -1;
            double mz = -1;
            if(l.get(0).getDim().equals("0")) {
                rt = Double.parseDouble(((String)l.get(0).getValue()));
            }else if(l.get(0).getDim().equals("1")) {
                mz = Double.parseDouble(((String)l.get(0).getValue()));
            }
            if(l.get(1).getDim().equals("0")) {
                rt = Double.parseDouble(((String)l.get(1).getValue()));
            }else if(l.get(1).getDim().equals("1")) {
                mz = Double.parseDouble(((String)l.get(1).getValue()));
            }

            double intens = ft.getIntensity();
            TreeMap<Double,Double> mzI = null;
            if(featureMap.containsKey(rt)) {
                mzI = featureMap.get(rt);
            }else{
                mzI = new TreeMap<Double,Double>();
                featureMap.put(rt, mzI);
            }
            if(mzI.containsKey(mz)) {
                System.err.println("For file: "+filename+": Warning: mass "+mz+" at rt "+rt+" already contained in map!");
            }
            mzI.put(mz,intens);
            features++;
        }
        ArrayDouble.D1 masses = new ArrayDouble.D1(features);
        ArrayDouble.D1 intens = new ArrayDouble.D1(features);
        ArrayDouble.D1 sat = new ArrayDouble.D1(featureMap.keySet().size());
        ArrayDouble.D1 tic = new ArrayDouble.D1(featureMap.keySet().size());
        ArrayDouble.D1 mr_min = new ArrayDouble.D1(featureMap.keySet().size());
        ArrayDouble.D1 mr_max = new ArrayDouble.D1(featureMap.keySet().size());
        ArrayInt.D1 sidx = new ArrayInt.D1(featureMap.keySet().size());
        int pointIdx = 0;
        int scanIdx = 0;
        for(Double d:featureMap.keySet()) {
            TreeMap<Double,Double> fmap = featureMap.get(d);
            double isum = 0;
            sidx.set(scanIdx, pointIdx);
            double minMZ = Double.POSITIVE_INFINITY;
            double maxMZ = Double.NEGATIVE_INFINITY;
            for(Double mz:fmap.keySet()) {
                Double ival = fmap.get(mz);
                isum+=ival;
                if(mz>maxMZ) {
                    maxMZ = mz;
                }
                if(mz<minMZ) {
                    minMZ = mz;
                }
                masses.set(pointIdx,mz);
                intens.set(pointIdx,ival);
                pointIdx++;
            }
            mr_min.set(scanIdx,minMZ);
            mr_max.set(scanIdx,maxMZ);
            sat.set(scanIdx, d);
            tic.set(scanIdx, isum);
            scanIdx++;
        }
        String fragName = StringTools.removeFileExt(filename)+".cdf";
        IFileFragment chromMS = new FileFragment(outputDir, fragName);
        VariableFragment mv = new VariableFragment(chromMS,"mass_values");
        Dimension pointNumber = new Dimension("point_number", features, true);
        mv.setDimensions(new Dimension[]{pointNumber});
        mv.setArray(masses);

        VariableFragment iv = new VariableFragment(chromMS,"intensity_values");
        iv.setDimensions(new Dimension[]{pointNumber});
        iv.setArray(intens);

        Dimension scanNumber = new Dimension("scan_number", featureMap.keySet().size(), true);
        VariableFragment satv = new VariableFragment(chromMS,"scan_acquisition_time");
        satv.setDimensions(new Dimension[]{scanNumber});
        satv.setArray(sat);

        VariableFragment siv = new VariableFragment(chromMS,"scan_index");
        siv.setDimensions(new Dimension[]{scanNumber});
        siv.setArray(sidx);

        VariableFragment ticv = new VariableFragment(chromMS,"total_intensity");
        ticv.setDimensions(new Dimension[]{scanNumber});
        ticv.setArray(tic);

        VariableFragment minMZv = new VariableFragment(chromMS,"mass_range_min");
        minMZv.setDimensions(new Dimension[]{scanNumber});
        minMZv.setArray(mr_min);

        VariableFragment maxMZv = new VariableFragment(chromMS,"mass_range_max");
        maxMZv.setDimensions(new Dimension[]{scanNumber});
        maxMZv.setArray(mr_max);

        chromMS.save();

        return chromMS;
    }

    public static void main(String[] args) {
        Factory fac = Factory.getInstance();
        fac.getConfiguration().setProperty("output.overwrite", true);
        //args[0] is output dir
        String output = args[0];
        //args[1] is input dir
        File inputDir = new File(args[1]);
        //args[2] is file suffix
        final String suff = args[2];
        File[] files = inputDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(suff);
            }
        });
        for(File f:files) {
            System.out.println("Processing file "+f.getName());
            FeatureMap.FeatureList fl = getFeatureList(f);
            IFileFragment frag = joinFeatures(new File(output),f.getName(),fl);
        }
        System.out.println("Done!");
        System.exit(0);
    }
}
