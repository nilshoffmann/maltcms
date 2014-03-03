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

import cross.datastructures.tuple.Tuple2D;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import maltcms.datastructures.ms.IMetabolite;

public class QueryResultListCellRenderer extends JLabel implements ListCellRenderer {

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
        Object o = list.getModel().getElementAt(index >= 0 ? index : 0);
        if (o instanceof Tuple2D<?, ?>) {
            Double d = -1.0d;
            IMetabolite m = null;
            if (((Tuple2D<?, ?>) o).getFirst() instanceof Double) {
                d = (Double) ((Tuple2D<?, ?>) o).getFirst();
            }
            if (((Tuple2D<?, ?>) o).getSecond() instanceof IMetabolite) {
                m = (IMetabolite) ((Tuple2D<?, ?>) o).getSecond();
            }
            if (m != null && d >= 0.0d) {
                StringBuilder sb = new StringBuilder();

                sb.append((index + 1));
                sb.append(": SCORE=");
                sb.append(String.format("%4.2f", d));
                if (m.getShortName() != null && !m.getShortName().isEmpty()) {
                    sb.append(" | NAME: ");
                    sb.append(m.getShortName());
                }
                sb.append(" | ID: ");
                sb.append(m.getID());
                setText(sb.toString());
            } else {
                System.err.println("Invalid Objects, not of Type Double or IMetabolite");
            }
        } else {
            System.err.println("Unknow Object type returned from list!");
        }

        return this;
    }
}
