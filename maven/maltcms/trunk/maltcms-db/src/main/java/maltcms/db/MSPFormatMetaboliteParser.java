package maltcms.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.datastructures.ms.Metabolite;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;

public class MSPFormatMetaboliteParser {

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
            // System.out.println("Skipping "+line);
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
            // System.out.println(data.toString());
        }
        linecounter++;
    }

    public void createMetabolite() {
        if (this.id == null) {
            this.id = this.name;
        }
        if (this.name == null) {
            System.err.println("Error creating metabolite, name=" + this.name
                    + "; id=" + this.id);
            System.exit(-1);
        }
        IMetabolite m = new Metabolite(this.name, this.id, this.idType,
                this.dbno, this.comments, this.formula, this.syndate, this.ri,
                this.rt, this.rtUnit, (int) this.mw, this.sp, this.synname,
                this.masses, this.intensities);
        if (m == null) {
            System.err.println("Error creating metabolite");
            System.exit(-1);
        }
        this.metabolites.add(m);
        System.out.println("Parsed Metabolite Nr. " + this.metabolites.size()
                + ": " + m.getName());
        System.out.println("Parsed Metabolite : " + m.toString());
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

    public void handleName(String name) {
        this.name = name;
    }

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
            // System.err.println("Unknown SYNON attribute: "+synon);
            // System.exit(-1);
        }
    }

    public void handleSynonDate(String date) {
        // System.out.println("Date: "+date);
        this.syndate = date;
    }

    public void handleSynonName(String name) {
        // System.out.println("Name: "+name);
        this.synname = name;
    }

    public void handleSynonSP(String sp) {
        // System.out.println("Synon: SP:"+sp);
        this.sp = sp;
    }

    public void handleSynonMPIMPID(String id) {
        // System.out.println("Synon: MPIMP-ID:"+id);
        handleSynonID(id);
    }

    public void handleSynonID(String id) {
        // System.out.println("Synon: ID:"+id);
        this.id = id;
    }

    public void handleSynonRI(String ri) {
        // System.out.println("Synon: RI:"+ri);
        this.ri = Double.parseDouble(ri);
    }

    public void handleSynonRT(String rt) {
        // System.out.println("Synon: RT:"+rt);
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

    public void handleSynonMATCH(String match) {
        System.out.println("IGNORING ATTRIBUTE MATCH=" + match);
    }

    public void handleFormula(String formula) {
        // System.out.println("formula: "+formula);
        this.formula = formula;
    }

    public void handleMW(String mw) {
        // System.out.println("MW: "+mw);
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

    public void handleComments(String comments) {
        // System.out.println("Comments: "+comments);
        this.comments = comments;
    }

    public void handleDBNo(String dbno) {
        // System.out.println("DB#: "+dbno);
        this.dbno = Integer.parseInt(dbno);
    }

    public void handleNumPeaks(String numpeaks) {
        // System.out.println("Num Peaks: "+numpeaks);
        this.npeaks = Integer.parseInt(numpeaks);
    }

    public void handleData(String data) {
        if (this.masses == null) {
            this.masses = new ArrayDouble.D1(this.npeaks);
        }
        if (this.intensities == null) {
            this.intensities = new ArrayInt.D1(this.npeaks);
        }
        // System.out.println(data);
        if (data.contains(";")) {
            parseMZI(data, " ", ";");
            // System.out.println(this.masses);
            // System.out.println(this.intensities);
        } else if (data.contains(" ")) {
            // System.out.println("Parsing msp compatible data!");
            parseMZI(data, ":", " ");
        } else {
            System.out.println("This is no valid data line! " + data);
        }
    }

    private void parseMZI(String data, String pairsep, String recordsep) {
        // System.out.println("Parsing mzI data: "+data);
        String[] pairs = data.split(recordsep);
        System.out.println("Point pairs: " + Arrays.deepToString(pairs));
        for (String p : pairs) {
            p = p.trim();
            if (!p.isEmpty()) {
                // System.out.println(p);
                String[] pair = p.split(pairsep);
                System.out.println(Arrays.toString(pair));
                if (pair.length == 2) {
                    this.masses.set(this.points, Double.parseDouble(pair[0]));
                    this.intensities.set(this.points, Integer.parseInt(pair[1]));
                    this.points++;
                } else {
                    System.err.println("Incorrect split result for pair: " + p
                            + "! Omitting rest!");
                    return;
                }
            }
        }
    }

    public ArrayList<IMetabolite> parse(File f) {
        this.metabolites = new ArrayList<IMetabolite>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = br.readLine()) != null) {
                handleLine(line);
            }
            // System.out.println("Found "+nnpeaks+" mass spectra!");
            // System.exit(-1);
            // handleLine("\r");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return this.metabolites;
    }

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
                    System.out.println("DB holding " + numMet.size()
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
                        System.out.println("Adding metabolite " + (i + 1) + "/"
                                + size + " :" + me.getName() + " to db!");
                        System.out.println("ID: " + ID);
                        db.store(me);
                        // }else if(os.size()==1) {
                        // System.out.println("Updating metabolite "+(i+1)+"/"+size+" :"+me.getName());
                        // os.get(0).update(me);
                        // db.set(me);
                        // } else if(os.size()>2){
                        // System.err.println("Query returned ambiguous results for "+(i+1)+"/"+size+"!");
                        // for(IMetabolite met:os) {
                        // System.err.println(met.getName());
                        // }
                        // System.exit(-1);
                        // }
                        i++;
                    }
                    cnt += i;
                    System.out.println("Committing to database!");
                    db.commit();
                }
                int committed = db.query(IMetabolite.class).size();
                System.out.println("Processed " + cnt
                        + " Metabolites, total in database: "
                        + committed + "!");
                db.close();

            } finally {
                db.close();
            }

        } else {
            System.out.println(
                    "Usage: MSPFormatMetaboliteParser <OUTFILE> <INFILE_1> ... <INFILE_N>");
        }
        System.exit(0);
    }
}
