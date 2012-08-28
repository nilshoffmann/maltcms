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
package net.sf.maltcms.evaluation.spi.hohenheim;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import jxl.Workbook;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.ext.DatabaseFileLockedException;
import com.db4o.ext.DatabaseReadOnlyException;
import com.db4o.ext.Db4oIOException;
import com.db4o.ext.IncompatibleFileFormatException;
import com.db4o.ext.OldFormatException;
import com.db4o.query.Predicate;

import cross.datastructures.tuple.Tuple2D;
import maltcms.io.csv.CSVReader;
import cross.tools.StringTools;
import net.sf.maltcms.evaluation.spi.xcalibur.Chromatogram;
import net.sf.maltcms.evaluation.spi.xcalibur.Creator;
import net.sf.maltcms.evaluation.spi.xcalibur.Peak;
import net.sf.maltcms.evaluation.spi.xcalibur.RTUnit;
import net.sf.maltcms.evaluation.spi.xcalibur.XLSIOProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
public class Eval {

    private final String dbdir;

    public Eval(String dbdir) {
        this.dbdir = dbdir;
    }

    public String getDBDir() {
        return this.dbdir;
    }

    public ObjectContainer createDB(boolean overwrite, String[] args) {

        ObjectContainer oc = createDatabase(getDBDir(), overwrite);
        if (overwrite) {
            for (String s : args) {
                File f = new File(s);
                String toolname = StringTools.removeFileExt(f.getName());
                List<String> peakFilesForTool = getFilesFromFile(s);
                if (toolname.equalsIgnoreCase("maltcms")) {
                    Creator c = new Creator(toolname, "0.95-beta");
                    oc.store(c);
                    System.out.println("Processing maltcms peaks");
                    for (String peakFile : peakFilesForTool) {
                        List<Peak> l = getMaltcmsPeaks(c, oc, peakFile);
                        System.out.println(
                                "Storing " + l.size() + " peaks in db");
                        for (Peak p : l) {
                            oc.store(p);
                        }
                    }
                } else if (toolname.equalsIgnoreCase("xcalibur")) {
                    System.out.println("Processing xcalibur peaks");
                    Creator c = new Creator("xcalibur", "unknown");
                    oc.store(c);
                    for (String peakFile : peakFilesForTool) {
                        List<Peak> l = getXcaliburPeaks(c, oc, peakFile);
                        System.out.println(
                                "Storing " + l.size() + " peaks in db");
                        for (Peak p : l) {
                            oc.store(p);
                        }
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported toolname: " + toolname);
                }
                System.out.println("Committing peaks to db");
                oc.commit();
            }
        }

        return oc;
    }

    public static void main(String[] args) {
        Eval e = new Eval(System.getProperty("user.dir"));
        ObjectContainer oc = e.createDB(false, args);
        System.out.println("Querying for peaks");
        ObjectSet<Peak> xcpeaks = e.getPeaks(oc);
        HashSet<String> peakNames = new HashSet<String>();
        HashMap<String, Double> rtToPeakNames = new HashMap<String, Double>();
        for (Peak p : xcpeaks) {
            System.out.println(p.getName());
            peakNames.add(p.getName());
            if (!rtToPeakNames.containsKey(p.getName())) {
                rtToPeakNames.put(p.getName(), p.getRt());
            }
        }
        List<String> peakNamesList = new ArrayList<String>(peakNames);
        Collections.sort(peakNamesList, e.new StringListByDoubleComparator(
                rtToPeakNames));
        System.out.println(peakNames);
        List<List<String>> table = new ArrayList<List<String>>();
        for (Chromatogram c : e.getChromatograms(oc)) {
            List<String> column = new ArrayList<String>();
            column.add(c.getName());
            System.out.println("Adding column " + c.getName());
            for (String peakName : peakNamesList) {
                Peak peak = e.getPeakForChromatogramByCreatorByName(oc, c,
                        "xcalibur", peakName).get(0);
                column.add((peak.getRt() / 60.0) + "");
            }
            table.add(column);
        }
        int nrows = peakNamesList.size() + 1;
        System.out.println(table);
        File xcfile = new File("xcalibur", "multiple-alignmentRT.csv");
        if (!xcfile.getParentFile().exists()) {
            xcfile.getParentFile().mkdirs();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(xcfile));
            for (int i = 0; i < nrows; i++) {
                StringBuilder row = new StringBuilder();
                for (int col = 0; col < table.size(); col++) {
                    row.append(table.get(col).get(i) + "\t");
                }
                row.append("\n");
                bw.write(row.toString());
            }
            bw.flush();
            bw.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public class StringListByDoubleComparator implements Comparator<String> {

        private final HashMap<String, Double> rtToPeakNames;

        public StringListByDoubleComparator(
                HashMap<String, Double> rtToPeakNames) {
            this.rtToPeakNames = rtToPeakNames;
        }

        @Override
        public int compare(String o1, String o2) {
            double s1 = rtToPeakNames.get(o1);
            double s2 = rtToPeakNames.get(o2);
            if (s1 > s2) {
                return 1;
            } else if (s1 < s2) {
                return -1;
            }
            return 0;
        }
    }

    public ObjectSet<Chromatogram> getChromatograms(ObjectContainer oc) {
        return oc.query(new ChromatogramPredicate());
    }

    public ObjectSet<Creator> getCreators(ObjectContainer oc) {
        return oc.query(new CreatorPredicate());
    }

    public ObjectSet<Peak> getPeaksForChromatogram(ObjectContainer oc,
            Chromatogram c) {
        return oc.query(new ChromatogramPeakPredicate(c));
    }

    public ObjectSet<Peak> getPeaksByCreator(ObjectContainer oc,
            String creatorname) {
        return oc.query(new CreatorPeakPredicate(creatorname));
    }

    public ObjectSet<Peak> getPeaksForChromatogramByCreator(ObjectContainer oc,
            Chromatogram c, String creatorname) {
        return oc.query(new ChromatogramCreatorPeakPredicate(c, creatorname));
    }

    public ObjectSet<Peak> getPeakForChromatogramByCreatorByName(
            ObjectContainer oc, Chromatogram c, String creatorname,
            String peakname) {
        return oc.query(new ChromatogramCreatorPeakNamePeakPredicate(c,
                creatorname, peakname));
    }

    public ObjectSet<Peak> getPeaks(ObjectContainer oc) {
        return oc.query(new PeakPredicate());
    }

    public class PeakPredicate extends Predicate<Peak> {

        /**
         *
         */
        private static final long serialVersionUID = -1948551337371019623L;

        @Override
        public boolean match(Peak arg0) {
            return true;
        }
    }

    public class CreatorPredicate extends Predicate<Creator> {

        /**
         *
         */
        private static final long serialVersionUID = -3543269480153995134L;
        private String name;

        public CreatorPredicate() {
        }

        public CreatorPredicate(String name) {
            this.name = name;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Creator arg0) {
            if (this.name == null) {
                return true;
            }
            return this.name.equals(arg0.getName());
        }
    }

    public class AggregatePeakPredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = -6754630453106703429L;
        private Predicate<Peak>[] p;

        public AggregatePeakPredicate(PeakPredicate... p) {
            this.p = p;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            boolean b = true;
            for (Predicate<Peak> pred : p) {
                b = pred.match(arg0);
                if (!b) {
                    return false;
                }
            }
            return b;
        }
    }

    public class ByCreatorNamePeakPredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final String name;

        public ByCreatorNamePeakPredicate(String name) {
            this.name = name;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            Array a = arg0.getFeature("CREATORNAME");
            String s = ((ArrayChar.D1) a).getString();
            return s.equals(this.name);
        }
    }

    public class ByCreatorNamePeakNamePredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = 6463510990923985730L;
        private final String name;
        private final String peakName;

        public ByCreatorNamePeakNamePredicate(String name, String peakName) {
            this.name = name;
            this.peakName = peakName;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            Array a = arg0.getFeature("CREATORNAME");
            String s = ((ArrayChar.D1) a).getString();
            Array b = arg0.getFeature("NAME");
            String t = ((ArrayChar.D1) b).getString();
            return s.equals(this.name) && t.equals(this.peakName);
        }
    }

    public class ChromatogramPredicate extends Predicate<Chromatogram> {

        /**
         *
         */
        private static final long serialVersionUID = -55691937902127499L;

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Chromatogram arg0) {
            return true;
        }
    }

    public class ChromatogramNamePredicate extends Predicate<Chromatogram> {

        /**
         *
         */
        private static final long serialVersionUID = -2493853621255366827L;
        private final String name;

        public ChromatogramNamePredicate(String name) {
            this.name = name;
        }
        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */

        @Override
        public boolean match(Chromatogram arg0) {
            if (this.name.equals(arg0.getName())) {
                return true;
            }
            return false;
        }
    }

    public class ChromatogramPeakPredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = 5009287857661674974L;
        private final String file;

        public ChromatogramPeakPredicate(Chromatogram c) {
            this.file = c.getName();
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            if (arg0.getParent().getName().equals(this.file)) {
                System.out.println("arg0: " + arg0.getParent().getName());
                System.out.println("this: " + this.file);
                return true;
            }
            return false;
        }
    }

    public class CreatorPeakPredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = -3734257468780932748L;
        private final String creator;

        public CreatorPeakPredicate(String creatorname) {
            this.creator = creatorname;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            if (((ArrayChar.D1) arg0.getFeature("CREATORNAME")).getString().
                    equals(this.creator)) {
                return true;
            }
            return false;
        }
    }

    public class ChromatogramCreatorPeakPredicate extends Predicate<Peak> {

        /**
         *
         */
        private static final long serialVersionUID = 5009287857661674974L;
        private final String file;
        private final String creator;

        public ChromatogramCreatorPeakPredicate(Chromatogram c, String creator) {
            this.file = c.getName();
            this.creator = creator;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
//        	System.out.println("arg0: "+arg0.getParent().getName()+" "+arg0.getCreatorname());
//        	System.out.println("this: "+this.file+" "+this.creator);
            if (arg0.getParent().getName().equals(this.file) && arg0.
                    getCreatorname().startsWith(this.creator)) {
                return true;
            }
            return false;
        }
    }

    public class ChromatogramCreatorPeakNamePeakPredicate extends Predicate<Peak> {

        /**
         *
         */
        private static final long serialVersionUID = 4469891680424542881L;
        /**
         *
         */
        private final String file;
        private final String creator;
        private final String peakname;

        public ChromatogramCreatorPeakNamePeakPredicate(Chromatogram c,
                String creator, String peakname) {
            this.file = c.getName();
            this.creator = creator;
            this.peakname = peakname;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
//        	System.out.println("arg0: "+arg0.getParent().getName()+" "+arg0.getCreatorname());
//        	System.out.println("this: "+this.file+" "+this.creator);
            if (arg0.getParent().getName().equals(this.file) && arg0.
                    getCreatorname().startsWith(this.creator) && arg0.getName().
                    equals(this.peakname)) {
                return true;
            }
            return false;
        }
    }

    public class ByCreatorNameFilenameRTPeakPredicate extends PeakPredicate {

        /**
         *
         */
        private static final long serialVersionUID = -3389850465308219316L;
        private final double rt, maxrtdev;
        private final String name;
        private final String chromname;

        public ByCreatorNameFilenameRTPeakPredicate(String name, Chromatogram c,
                double rt, double maxrtdev) {
            this.name = name;
            this.chromname = c.getName();
            this.rt = rt;
            this.maxrtdev = maxrtdev;
        }

        /* (non-Javadoc)
         * @see com.db4o.query.Predicate#match(java.lang.Object)
         */
        @Override
        public boolean match(Peak arg0) {
            Array a = arg0.getFeature("RT");
            double d = ((ArrayDouble.D0) a).get();

            Array cna = arg0.getFeature("CREATORNAME");
            String cname = ((ArrayChar.D1) cna).getString();

            Array c = arg0.getFeature("FILE");
            String f = ((ArrayChar.D1) c).getString();
            return (Math.abs(d - this.rt) <= this.maxrtdev) && cname.equals(
                    this.name) && f.equals(this.chromname);
        }
    }

    public List<String> getFilesFromFile(String filesFile) {
        CSVReader csvr = new CSVReader();
        csvr.setFirstLineHeaders(false);
        csvr.setSkipCommentLines(true);
        csvr.setComment("#");
        csvr.setFieldSeparator("\t");
        try {
            Tuple2D<Vector<Vector<String>>, Vector<String>> t = csvr.read(new DataInputStream(new FileInputStream(new File(
                    filesFile))));
            Vector<Vector<String>> table = t.getFirst();
            ArrayList<String> files = new ArrayList<String>();
            for (Vector<String> row : table) {
                files.add(row.get(0));
            }
            return files;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public ObjectContainer createDatabase(String dbdir, boolean overwrite) {
        try {
            File db = new File(dbdir, "hohenheimEvalDB.db4o");
            if (overwrite && db.exists()) {
                db.delete();
            }
            Configuration cfg = Db4o.newConfiguration();
            ObjectContainer oc = Db4o.openFile(cfg, db.getAbsolutePath());
            return oc;
        } catch (Db4oIOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DatabaseFileLockedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibleFileFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OldFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DatabaseReadOnlyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new NullPointerException();
    }

    class PeakComparator implements Comparator<Peak> {

        @Override
        public int compare(Peak o1, Peak o2) {
            final Array lhs = o1.getFeature("RT");
            final Array rhs = o2.getFeature("RT");
            final Index lhsIdx = Index.scalarIndexImmutable;
            final Index rhsIdx = Index.scalarIndexImmutable;
            final double lhsv = lhs.getDouble(lhsIdx);
            final double rhsv = rhs.getDouble(rhsIdx);
            if (lhsv < rhsv) {
                return -1;
            } else if (lhsv > rhsv) {
                return 1;
            }
            return 0;

        }
    }

    public List<Peak> getXcaliburPeaks(Creator creator, ObjectContainer oc,
            String... files) {

        List<Peak> v = new ArrayList<Peak>();
        for (String file : files) {
            Workbook w = XLSIOProvider.getWorkbook(new File(file));
            Vector<String> filenames = XLSIOProvider.getFilenames(w);
            int rows = filenames.size();
            v.addAll(XLSIOProvider.getPeaks(w, rows, creator, oc));
        }
        return v;
    }

    public List<Peak> getMaltcmsPeaks(Creator creator, ObjectContainer oc,
            String... files) {
        List<Peak> v = new ArrayList<Peak>();
        for (String file : files) {
            CSVReader csvr = new CSVReader();
            Tuple2D<Vector<Vector<String>>, Vector<String>> t = csvr.read(file);
            File f = new File(file);
            String filename = f.getName().substring(0, f.getName().indexOf(
                    "_peakAreas.csv"));
            Chromatogram c = new Chromatogram(filename);
            ObjectSet<Chromatogram> os = oc.queryByExample(c);
            if (os.isEmpty()) {
                oc.store(c);
            } else {
                c = os.get(0);
            }
//			"APEX", "START", "STOP","RT_APEX", "RT_START", "RT_STOP", "AREA", "MW", "INTENSITY"
            Vector<Vector<String>> rows = t.getFirst();
            for (Vector<String> row : rows) {
                double rtapex = Double.NaN;
                double rtstart = Double.NaN;
                double rtstop = Double.NaN;
                double area = Double.NaN;
                double mw = Double.NaN;
                double intensity = Double.NaN;
                String fieldName = "";
                for (int i = 0; i < t.getSecond().size(); i++) {
                    fieldName = t.getSecond().get(i);
                    String str = row.get(i);
                    double val = parseDouble(str.trim());
                    if (fieldName.equals("RT_APEX")) {
                        rtapex = val;
                    }
                    if (fieldName.equals("RT_START")) {
                        rtstart = val;
                    }
                    if (fieldName.equals("RT_STOP")) {
                        rtstop = val;
                    }
                    if (fieldName.equals("AREA")) {
                        area = val;
                    }
                    if (fieldName.equals("MW")) {
                        mw = val;
                    }
                    if (fieldName.equals("INTENSITY")) {
                        intensity = val;
                    }
                }
                Peak p = new Peak(creator, c, "NN", rtapex, rtstart, rtstop,
                        new double[]{mw}, area, intensity, RTUnit.Seconds);
                System.out.println(p);
                v.add(p);
            }
        }
        return v;
    }

    public double parseDouble(String s) {
        if (s.isEmpty() || s.equals("N/A") || s.equals("N/F") || s.equals(
                "Peak Not Found")) {
            return Double.NaN;
        }
        s = s.replace(",", ".");
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException nfe) {
            System.err.println(nfe);
            return Double.NaN;
        }
    }
}
