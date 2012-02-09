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
package maltcms.db.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import maltcms.datastructures.ms.IMetabolite;
import maltcms.tools.PublicMemberGetters;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

public class MetaboliteViewModel extends AbstractTableModel {

    public final String COMMENTS = "Comments";
    public final String DATE = "Date";
    public final String FORMULA = "Formula";
    public final String ID = "ID";
    public final String MASSWEIGHT = "MW";
    public final String MASSSPECTRUM = "MassSpectrum";
    public final String MAXINTENSITY = "MaxIntensity";
    public final String MAXINTNORM = "MaxIntensityNormalized";
    public final String MAXMASS = "MaxMass";
    public final String MININTENSITY = "MinIntensity";
    public final String MININTNORM = "MinIntensityNormalized";
    public final String MINMASS = "MinMass";
    public final String NAME = "Name";
    public final String RETINDEX = "RetentionIndex";
    public final String RETTIME = "RetentionTime";
    public final String RETTIMEUNIT = "RetentionTimeUnit";
    public final String SP = "SP";
    public final String SCANINDEX = "ScanIndex";
    public final String SHORTNAME = "ShortName";
    /**
     *
     */
    private static final long serialVersionUID = -38817321732051832L;
    private PublicMemberGetters<IMetabolite> pmg = null;
    private String[] tableHeader = new String[]{};
    private Vector<IMetabolite> elements = null;
    private TreeMap<String, IMetabolite> map = new TreeMap<String, IMetabolite>();
    private ObjectContainer oc = null;
    private ExecutorService s = Executors.newFixedThreadPool(1);
    private MetaboliteView mv = null;
    private boolean[] headerVisible = null;

    public MetaboliteViewModel(ObjectContainer oc) {
        this.oc = oc;
        elements = new Vector<IMetabolite>();
        pmg = new PublicMemberGetters<IMetabolite>(IMetabolite.class);
        this.tableHeader = pmg.getGetterNames();
        this.headerVisible = new boolean[this.tableHeader.length];
        for (int i = 0; i < this.headerVisible.length; i++) {
            this.headerVisible[i] = true;
        }
        for (IMetabolite m : elements) {
            map.put(m.getID(), m);
        }
//		Collections.sort(elements,new Comparator<IMetabolite>() {
//		
//			
//			public int compare(IMetabolite arg0, IMetabolite arg1) {
//				if(arg0.getRetentionIndex()<arg1.getRetentionIndex()){
//					return -1;
//				}
//				if(arg0.getRetentionIndex()>arg1.getRetentionIndex()) {
//					return 1;
//				}
//				return 0;
//			}
//		
//		});

        //setDataVector(elements, DefaultTableModel.convertToVector(tableHeader));
        for (Object o : tableHeader) {
            System.out.println(o);
        }

//		query(new Predicate<IMetabolite>() {
//		
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 2435586279861149123L;
//
//			@Override
//			public boolean match(IMetabolite arg0) {
//				return true;
//			}
//		
//		});
//		elements.add(new Metabolite("Fructose","FRUC","",1,"","","",1,10,"",0,"","",null,null));
//		elements.add(new Metabolite("Undecane","UNDEC","",2,"","","",1,10,"",0,"","",null,null));
//		elements.add(new Metabolite("Eicosane","EICOS","",3,"","","",1,10,"",0,"","",null,null));
//		elements.add(new Metabolite("Glucose","GLUC","",4,"","","",1,10,"",0,"","",null,null));
//		elements.add(new Metabolite("Galactose","GALA","",5,"","","",1,10,"",0,"","",null,null));
    }

    private int getNumberOfHiddenColumns() {
        int cnt = 0;
        for (int i = 0; i < this.headerVisible.length; i++) {
            if (this.headerVisible[i] == false) {
                cnt++;
            }
        }
        return cnt;
    }

    private int getColumnForName(String name) {
        int res = java.util.Arrays.binarySearch(this.tableHeader, name);
        if (res >= 0) {
            return res;
        } else {
            return -1;
        }
    }

    public void setMetaboliteView(MetaboliteView mv) {
        this.mv = mv;
    }

    //HIER----------------------------------------------------------------------------
    public void setTableColumnVisible(String header, boolean b) {
        //System.out.println("Visible table headers: "+Arrays.toString(headers));
        //System.out.println("All table headers: "+Arrays.toString(tableHeader));
        int col = getColumnForName(header);
        if (col >= 0) {
            this.headerVisible[col] = b;
            fireTableStructureChanged();
        }
        //insert all valid header names
//		Set<String> diff = new HashSet<String>(java.util.Arrays.asList(tableHeader));
////		for(int i=0;i<this.tableHeader.length;i++) {
////			this.headerVisible[i] = true;
////		}
//		//remove all header names, that should be displayed
//		diff.removeAll(java.util.Arrays.asList(headers));
//		//set remaining header names to invisible
//		for(String s:diff){
//			int ip = java.util.Arrays.binarySearch(tableHeader,s);
//			this.headerVisible[ip] = false;
//		}
        //tableHeader = headers;
        //tableHeader = pmg.getGetterNames(headers);
        //System.out.println("New table header: "+Arrays.toString(tableHeader));
        //setColumnCount(headers.length);
        //setDataVector(elements, DefaultTableModel.convertToVector(tableHeader));
    }

//	@Override
//    public Class<?> getColumnClass(int columnIndex) {
//		Method m = pmg.getMethodForGetterName(tableHeader[columnIndex]);
//		return m.getReturnType();
//    }
    public String[] getTableHeader() {
        return tableHeader;
    }

    public String[] getVisibleTableHeader() {
        String[] s = new String[this.tableHeader.length - getNumberOfHiddenColumns()];
        for (int i = 0; i < s.length; i++) {
            s[i] = getColumnName(i);
        }
        return s;
    }

    @Override
    public String getColumnName(int column) {
        return tableHeader[getVisibleColumn(column)];
    }

    public ObjectContainer getObjectContainer() {
        return oc;
    }

    public void query(final Predicate<IMetabolite> p) {
        this.elements.clear();
        if (mv != null) {
            mv.setEnabled(false);
        }
        Runnable r = new Runnable() {

            public void run() {
                System.out.println("Running query with predicate of type: " + p.getClass().getCanonicalName());
                final ObjectSet<IMetabolite> os = oc.query(p);//IMetabolite.class);
                //setTableHeader(new String[]{"Name","RetentionIndex","Formula"}); //---------------
//				IMetabolite[] mets = new IMetabolite[5];
//				mets[0] = new Metabolite("Fructose","FRUC","",1,"","","",1,10,"",0,"","",null,null);
//				mets[1] = new Metabolite("Undecane","UNDEC","",2,"","","",1,10,"",0,"","",null,null);
//				mets[2] = new Metabolite("Eicosane","EICOS","",3,"","","",1,10,"",0,"","",null,null);
//				mets[3] = new Metabolite("Glucose","GLUC","",4,"","","",1,10,"",0,"","",null,null);
//				mets[4] = new Metabolite("Galactose","GALA","",5,"","","",1,10,"",0,"","",null,null);

                int i = 0;
                final int maxcnt = 500;
                int last = 0;
                System.out.println("Query returned " + os.size() + " results!");
                map.clear();
                final int rows = getRowCount();
                elements.clear();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        fireTableRowsDeleted(0, Math.max(0, rows - 1));

                    }
                });

                for (IMetabolite m : os) {
                    //System.out.println("Inserting "+m.getName()+" at position "+i);
                    map.put(m.getID(), m);
                    elements.add(m);
                    //elements.add(m);
                    //fireTableDataChanged();
                    final int current_row = i;
                    if (i % maxcnt == 0) {
                        last = -1;
                        Runnable notifier = new Runnable() {

                            public void run() {
                                System.out.println("Running table update from " + (current_row) + " to " + (current_row + maxcnt));
                                fireTableRowsInserted(current_row, current_row + maxcnt);//ContentsChanged(m, i, i);
                                //fireTableDataChanged();
                            }
                        };
                        SwingUtilities.invokeLater(notifier);
                    } else {
                        last = i / maxcnt;
                    }

                    i++;
                }
                final int remain = last * maxcnt;
                Runnable notifier = new Runnable() {

                    public void run() {
                        if (remain > 0) {
                            System.out.println("Running final table update from " + (remain) + " to " + elements.size());
                            fireTableRowsInserted(remain, elements.size());//ContentsChanged(m, i, i);
                        }
                        //ensure update table
                        fireTableDataChanged();
                        //enable table for selection
                        if (mv != null) {
                            mv.setEnabled(true);
                        }
                    }
                };
                SwingUtilities.invokeLater(notifier);
            }
        };

        s.submit(r);

    }

    private int getVisibleColumn(int arg1) {
        int count = 0;
        for (int index = 0; index < this.tableHeader.length; index++) {
            if (this.headerVisible[index]) {
                count++;
            }

            if (count - 1 == arg1) {
                return index;
            }
        }
        return 0;
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        //return super.getValueAt(arg0, arg1);
        if (arg0 < elements.size() && arg1 < (tableHeader.length)) {
            //System.out.println("GetValueAt arg0="+arg0+" arg1="+arg1);
            Method m = pmg.getMethodForGetterName(tableHeader[getVisibleColumn(arg1)]);
            if (m == null) {
                System.err.println("Invalid method name: " + tableHeader[getVisibleColumn(arg1)]);
            } else {
                try {
                    IMetabolite met = (IMetabolite) elements.get(arg0);
                    return m.invoke(met, new Object[]{});
                } catch (InvocationTargetException ite) {
                    System.err.println(ite.getLocalizedMessage());
                } catch (IllegalAccessException iae) {
                    System.err.println(iae.getLocalizedMessage());
                }
            }
        }
        if (arg0 < elements.size() && arg1 == -1) {
            return elements.get(arg0);
        }
        return "";
    }

    public IMetabolite getMetaboliteAtRow(int arg0) {
        if (arg0 < elements.size() && arg0 >= 0) {
            return elements.get(arg0);
        }
        return null;
    }

    public Method getMethodForGetterName(String name) {
        Method method = pmg.getMethodForGetterName(name);
        return method;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return this.tableHeader.length - getNumberOfHiddenColumns();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return this.elements.size();
    }
}
