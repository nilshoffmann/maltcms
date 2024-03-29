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
package maltcms.db;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import org.slf4j.LoggerFactory;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

/**
 * <p>MSPFormatMetaboliteParser class.</p>
 *
 * @author Nils Hoffmann
 * 
 */

public class MSPFormatMetaboliteParser {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MSPFormatMetaboliteParser.class);

    private ArrayDouble.D1 masses = null;
    private ArrayInt.D1 intensities = null;
    private String name = null;
    private StringBuffer data = null;
    private int dbno = -1;
    private String id = null;
    private String idType = null;
    private String comments = null;
    private int npeaks = 0;
    private int points = 0;
    private int linecounter = 0;
    private ArrayList<IMetabolite> metabolites;
    private String syndate;
    private String synname;
    private String sp;
    private double ri;
    private double rt;
    private String rtUnit;
    private String formula;
    private double mw;
    private boolean nextIsPeakData = false;
    private int nnpeaks = 0;

    /**
     * <p>handleLine.</p>
     *
     * @param line a {@link java.lang.String} object.
     */
    public void handleLine(String line) {
        if (line.startsWith("Name: ")) {
            handleName(line.substring("Name: ".length()));
        } else if (line.startsWith("Synon: ")) {
            handleSynon(line.substring("Synon: ".length()));
        } else if (line.startsWith("DB#: ")) {
            handleDBNo(line.substring("DB#: ".length()));
        } else if (line.startsWith("Comment: ")) {
            handleComments(line.substring("Comment: ".length()));
        } else if (line.startsWith("Comments: ")) {
            handleComments(line.substring("Comments: ".length()));
        } else if (line.startsWith("CAS#: ")) {
            // log.info("Skipping "+line);
        } else if (line.startsWith("Num Peaks: ")) {
            nnpeaks++;
            handleNumPeaks(line.substring(("Num Peaks: ").length()));
            nextIsPeakData = true;
        } else if (line.startsWith("Formula: ")) {
            handleFormula(line.substring(("Formula: ").length()));
        } else if (line.startsWith("MW: ")) {
            handleMW(line.substring(("MW: ").length()));
        } else if (line.isEmpty()) {// next metabolite
            if (data != null) {
                handleData(data.toString());
                createMetabolite();
            }
        } else {// we have data
            if (nextIsPeakData) {
                if (data == null) {
                    data = new StringBuffer();
                }
                data.append(line);
            }
            // log.info(data.toString());
        }
        linecounter++;
    }

    /**
     * <p>createMetabolite.</p>
     */
    public void createMetabolite() {
        if (this.id == null) {
            this.id = this.name;
        }
        if (this.name == null) {
            log.warn("Error creating metabolite, name=" + this.name
                    + "; id=" + this.id);
            System.exit(-1);
        }
        IMetabolite m = new Metabolite(this.name, this.id, this.idType,
                this.dbno, this.comments, this.formula, this.syndate, this.ri,
                this.rt, this.rtUnit, (int) this.mw, this.sp, this.synname,
                this.masses, this.intensities);
        if (m == null) {
            log.warn("Error creating metabolite");
            System.exit(-1);
        }
        this.metabolites.add(m);
        log.info("Parsed Metabolite Nr. " + this.metabolites.size()
                + ": " + m.getName());
        log.info("Parsed Metabolite : " + m.toString());
        this.name = null;
        this.id = null;
        this.idType = null;
        this.dbno = -1;
        this.comments = null;
        this.syndate = null;
        this.ri = 0;
        this.sp = null;
        this.synname = null;
        this.masses = null;
        this.intensities = null;
        this.points = 0;
        this.mw = 0;
        this.rt = 0;
        this.rtUnit = null;
        this.formula = null;
        this.npeaks = 0;
        this.data = null;
        this.nextIsPeakData = false;
    }

    /**
     * <p>handleName.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void handleName(String name) {
        this.name = name;
    }

    /**
     * <p>handleSynon.</p>
     *
     * @param synon a {@link java.lang.String} object.
     */
    public void handleSynon(String synon) {
        if (synon.startsWith("DATE:")) {
            handleSynonDate(synon.substring("DATE:".length()));
        } else if (synon.startsWith("NAME:")) {
            handleSynonName(synon.substring("NAME:".length()));
        } else if (synon.startsWith("SP:")) {
            handleSynonSP(synon.substring("SP:".length()));
        } else if (synon.startsWith("CHROMA4D-ID:")) {
            this.idType = "CHROMA4D-ID";
            handleSynonID(synon.substring("CHROMA4D-ID:".length()));
        } else if (synon.startsWith("MPIMP-ID:")) {
            this.idType = "MPIMP-ID";
            handleSynonMPIMPID(synon.substring("MPIMP-ID:".length()));
        } else if (synon.startsWith("ID:")) {
            this.idType = "ID";
            handleSynonID(synon.substring("ID:".length()));
        } else if (synon.startsWith("RI:")) {
            handleSynonRI(synon.substring(("RI:").length()));
        } else if (synon.startsWith("RT:")) {
            handleSynonRT(synon.substring(("RT:").length()));
        } else if (synon.startsWith("MATCH:")) {
            handleSynonMATCH(synon.substring(("MATCH:").length()));
        } else {
            // log.warn("Unknown SYNON attribute: "+synon);
            // System.exit(-1);
        }
    }

    /**
     * <p>handleSynonDate.</p>
     *
     * @param date a {@link java.lang.String} object.
     */
    public void handleSynonDate(String date) {
        // log.info("Date: "+date);
        this.syndate = date;
    }

    /**
     * <p>handleSynonName.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void handleSynonName(String name) {
        // log.info("Name: "+name);
        this.synname = name;
    }

    /**
     * <p>handleSynonSP.</p>
     *
     * @param sp a {@link java.lang.String} object.
     */
    public void handleSynonSP(String sp) {
        // log.info("Synon: SP:"+sp);
        this.sp = sp;
    }

    /**
     * <p>handleSynonMPIMPID.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void handleSynonMPIMPID(String id) {
        // log.info("Synon: MPIMP-ID:"+id);
        handleSynonID(id);
    }

    /**
     * <p>handleSynonID.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void handleSynonID(String id) {
        // log.info("Synon: ID:"+id);
        this.id = id;
    }

    /**
     * <p>handleSynonRI.</p>
     *
     * @param ri a {@link java.lang.String} object.
     */
    public void handleSynonRI(String ri) {
        // log.info("Synon: RI:"+ri);
        this.ri = Double.parseDouble(ri);
    }

    /**
     * <p>handleSynonRT.</p>
     *
     * @param rt a {@link java.lang.String} object.
     */
    public void handleSynonRT(String rt) {
        // log.info("Synon: RT:"+rt);
        // this.rt = Double.parseDouble(rt);
        // Assume sec or min prefix
        if (rt.startsWith("sec") || rt.startsWith("min")) {
            this.rtUnit = rt.substring(0, 3);
            this.rt = Double.parseDouble(rt.substring(4));
        } else {
            this.rtUnit = "sec";
            this.rt = Double.parseDouble(rt);
        }

    }

    /**
     * <p>handleSynonMATCH.</p>
     *
     * @param match a {@link java.lang.String} object.
     */
    public void handleSynonMATCH(String match) {
        log.info("IGNORING ATTRIBUTE MATCH=" + match);
    }

    /**
     * <p>handleFormula.</p>
     *
     * @param formula a {@link java.lang.String} object.
     */
    public void handleFormula(String formula) {
        // log.info("formula: "+formula);
        this.formula = formula;
    }

    /**
     * <p>handleMW.</p>
     *
     * @param mw a {@link java.lang.String} object.
     */
    public void handleMW(String mw) {
        // log.info("MW: "+mw);
        try {
            this.mw = Double.parseDouble(mw);
        } catch (NumberFormatException nfe) {
            if (mw.indexOf(",") < mw.indexOf(".")) {
                String tmp = mw.replace(",", "");
                // tmp = tmp.replace(",", ".");
                this.mw = Double.parseDouble(tmp);
            } else {
                this.mw = Double.NaN;
            }
        }
    }

    /**
     * <p>handleComments.</p>
     *
     * @param comments a {@link java.lang.String} object.
     */
    public void handleComments(String comments) {
        // log.info("Comments: "+comments);
        this.comments = comments;
    }

    /**
     * <p>handleDBNo.</p>
     *
     * @param dbno a {@link java.lang.String} object.
     */
    public void handleDBNo(String dbno) {
        // log.info("DB#: "+dbno);
        this.dbno = Integer.parseInt(dbno);
    }

    /**
     * <p>handleNumPeaks.</p>
     *
     * @param numpeaks a {@link java.lang.String} object.
     */
    public void handleNumPeaks(String numpeaks) {
        // log.info("Num Peaks: "+numpeaks);
        this.npeaks = Integer.parseInt(numpeaks);
    }

    /**
     * <p>handleData.</p>
     *
     * @param data a {@link java.lang.String} object.
     */
    public void handleData(String data) {
        if (this.masses == null) {
            this.masses = new ArrayDouble.D1(this.npeaks);
        }
        if (this.intensities == null) {
            this.intensities = new ArrayInt.D1(this.npeaks);
        }
        // log.info(data);
        if (data.contains(";")) {
            parseMZI(data, " ", ";");
            // log.info(this.masses);
            // log.info(this.intensities);
        } else if (data.contains(" ")) {
            // log.info("Parsing msp compatible data!");
            parseMZI(data, ":", " ");
        } else {
            log.info("This is no valid data line! " + data);
        }
    }

    private void parseMZI(String data, String pairsep, String recordsep) {
        // log.info("Parsing mzI data: "+data);
        String[] pairs = data.split(recordsep);
        log.info("Point pairs: " + Arrays.deepToString(pairs));
        for (String p : pairs) {
            p = p.trim();
            if (!p.isEmpty()) {
                // log.info(p);
                String[] pair = p.split(pairsep);
                log.info(Arrays.toString(pair));
                if (pair.length == 2) {
                    this.masses.set(this.points, Double.parseDouble(pair[0]));
                    this.intensities.set(this.points, Integer.parseInt(pair[1]));
                    this.points++;
                } else {
                    log.warn("Incorrect split result for pair: " + p
                            + "! Omitting rest!");
                    return;
                }
            }
        }
    }

    /**
     * <p>parse.</p>
     *
     * @param f a {@link java.io.File} object.
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<IMetabolite> parse(File f) {
        this.metabolites = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br.readLine()) != null) {
                handleLine(line);
            }
            // log.info("Found "+nnpeaks+" mass spectra!");
            // System.exit(-1);
            // handleLine("\r");
        } catch (FileNotFoundException e) {
            log.warn(e.getLocalizedMessage());
            log.warn(e.getLocalizedMessage());
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
            log.warn(e.getLocalizedMessage());
        }
        return this.metabolites;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {
        MSPFormatMetaboliteParser gmp = new MSPFormatMetaboliteParser();
        if (args.length > 1) {
            ObjectContainer db = Db4o.openFile(args[0]);
            Configuration configuration = Db4o.newConfiguration();
            configuration.objectClass("maltcms.ms.IMetabolite").cascadeOnUpdate(
                    true);
            String[] files = new String[args.length - 1];
            System.arraycopy(args, 1, files, 0, files.length);
            try {
                int cnt = 0;
                for (String s : files) {
                    File f = new File(s);
                    ArrayList<IMetabolite> al = gmp.parse(f);
                    ObjectSet<IMetabolite> numMet = db.query(new Predicate<IMetabolite>() {
                        /**
                         *
                         */
                        private static final long serialVersionUID = -3537350989202183850L;

                        @Override
                        public boolean match(IMetabolite arg0) {
                            return true;
                        }
                    });
                    log.info("DB holding " + numMet.size()
                            + " metabolites!");
                    int i = 0;
                    int size = al.size();
                    for (IMetabolite me : al) {
                        final String ID = me.getID();
                        // ObjectSet<IMetabolite> os = db.query(new
                        // Predicate<IMetabolite>() {

                        /**
                         *
                         */
                        // private static final long serialVersionUID =
                        // -8580415202887162014L;
                        // @Override
                        // public boolean match(IMetabolite arg0) {
                        // if(arg0.getID().equals(ID)){
                        // return true;
                        // }
                        // return false;
                        // }
                        // });
                        // if(os.size() == 0) {
                        log.info("Adding metabolite " + (i + 1) + "/"
                                + size + " :" + me.getName() + " to db!");
                        log.info("ID: " + ID);
                        db.store(me);
                        // }else if(os.size()==1) {
                        // log.info("Updating metabolite "+(i+1)+"/"+size+" :"+me.getName());
                        // os.get(0).update(me);
                        // db.set(me);
                        // } else if(os.size()>2){
                        // log.warn("Query returned ambiguous results for "+(i+1)+"/"+size+"!");
                        // for(IMetabolite met:os) {
                        // log.warn(met.getName());
                        // }
                        // System.exit(-1);
                        // }
                        i++;
                    }
                    cnt += i;
                    log.info("Committing to database!");
                    db.commit();
                }
                int committed = db.query(IMetabolite.class).size();
                log.info("Processed " + cnt
                        + " Metabolites, total in database: "
                        + committed + "!");
                db.close();

            } finally {
                db.close();
            }

        } else {
            log.info(
                    "Usage: MSPFormatMetaboliteParser <OUTFILE> <INFILE_1> ... <INFILE_N>");
        }
        System.exit(0);
    }
}
