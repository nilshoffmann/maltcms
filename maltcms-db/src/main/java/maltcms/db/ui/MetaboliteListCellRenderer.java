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
package maltcms.db.ui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import maltcms.datastructures.ms.IMetabolite;

/**
 * <p>MetaboliteListCellRenderer class.</p>
 *
 * @author Nils Hoffmann
 * 
 */
public class MetaboliteListCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * <p>Constructor for MetaboliteListCellRenderer.</p>
     */
    public MetaboliteListCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }
    /**
     *
     */
    private static final long serialVersionUID = -2284912996792720438L;

    /** {@inheritDoc} */
    @Override
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
        Object o = list.getModel().getElementAt(index >= 0 ? index : 0);
        if (o instanceof IMetabolite) {
            IMetabolite m = (IMetabolite) o;
            if (m.getShortName() != null && !m.getShortName().isEmpty()) {
                setText((index + 1) + " " + m.getShortName());
            } else {
                setText((index + 1) + " " + m.getID());
            }
        }

        return this;
    }
}
