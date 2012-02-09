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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import maltcms.datastructures.ms.IMetabolite;
import cross.datastructures.tuple.Tuple2D;

public class QueryResultListCellRenderer extends JLabel implements ListCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5980514459833450228L;

	public QueryResultListCellRenderer() {
		setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
	}

	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        //Set the icon and text.  If icon was null, say so.
        Object o = list.getModel().getElementAt(index>=0?index:0);
        if(o instanceof Tuple2D<?,?>) {
        	Double d = -1.0d;
        	IMetabolite m = null;
        	if(((Tuple2D<?,?>)o).getFirst() instanceof Double){
        		d = (Double)((Tuple2D<?,?>)o).getFirst();
        	}
        	if(((Tuple2D<?,?>)o).getSecond() instanceof IMetabolite){
        		m = (IMetabolite)((Tuple2D<?,?>)o).getSecond();
        	}
        	if(m!=null && d >= 0.0d){
        		StringBuilder sb = new StringBuilder();
	        	
	        		sb.append((index+1));
	        		sb.append(": SCORE=");
	        		sb.append(String.format("%4.2f", d));
	        	if(m.getShortName()!=null && !m.getShortName().isEmpty()) {
	        		sb.append(" | NAME: ");
	        		sb.append(m.getShortName());
	        	}
	        		sb.append(" | ID: ");
	        		sb.append(m.getID());
	        		setText(sb.toString()); 
        	}else{
        		System.err.println("Invalid Objects, not of Type Double or IMetabolite");
        	}
        }else{
        	System.err.println("Unknow Object type returned from list!");
        }
        

        return this;
	}
	
}