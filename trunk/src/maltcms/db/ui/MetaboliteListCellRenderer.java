/**
 * 
 */
package maltcms.db.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import maltcms.datastructures.ms.IMetabolite;

public class MetaboliteListCellRenderer extends JLabel implements ListCellRenderer{

	public MetaboliteListCellRenderer() {
		setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2284912996792720438L;

	
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
        if(o instanceof IMetabolite) {
        	IMetabolite m = (IMetabolite)o;
        	if(m.getShortName()!=null && !m.getShortName().isEmpty()) {
        		setText((index+1)+" "+m.getShortName());
        	} else {
        		setText((index+1)+" "+m.getID());
        	} 
        }

        return this;
	}
	
}