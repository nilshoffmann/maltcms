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
package maltcms.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import maltcms.datastructures.alignment.AnchorPairSet;
import maltcms.datastructures.array.PartitionedArray;
import cross.datastructures.tuple.Tuple2D;
import cross.datastructures.tools.EvalTools;

/**
 * Visual class displaying the proposed partitioning of an array with a given
 * set of anchors.
 * 
 * @author Nils.Hoffmann@cebitec.uni-bielefeld.de
 * 
 */
public class TabbedPanel extends JPanel implements ActionListener {

	protected class RepaintRunnable implements Runnable {

		private Graphics2D g2 = null;

		public void run() {
			if ((this.g2 != null) && (getContent() != null)) {
				final BufferedImage bi = getContent();
				this.g2.drawImage(bi, 0, 0, getWidth(), getHeight(), 0, 0, bi
				        .getWidth(), bi.getHeight(), null);
				this.g2.dispose();
			}
		}

		public void setGraphics(final Graphics g) {
			this.g2 = (Graphics2D) g;
		}

	}

	/**
     * 
     */
	private static final long serialVersionUID = 7611857256393422827L;

	// private double zoom = 1.0d;
	private PartitionedArray pa = null;

	private Dimension paSize = null;

	private BufferedImage paIm = null;

	private RepaintRunnable rr = null;

	private AnchorPairSet aps = null;

	public TabbedPanel(final PartitionedArray pa1, final AnchorPairSet aps1) {
		this.pa = pa1;
		this.aps = aps1;
		this.paSize = pa1.getEnclosingRectangle().getSize();
		this.paIm = new BufferedImage(this.paSize.width, this.paSize.height,
		        BufferedImage.TYPE_INT_ARGB);
		createPAIm(this.paIm);
	}

	public void actionPerformed(final ActionEvent evt) {
		// System.out.println("IEvent received!");
		// System.out.println(evt.toString());
		// System.out.println(this.zoom);
		// if(evt.getActionCommand().equals("ZOOMIN")) {
		// this.zoom = (this.zoom<1000.0d)?this.zoom*2.0d:this.zoom;
		// }else if (evt.getActionCommand().equals("ZOOMOUT")) {
		// this.zoom = (this.zoom>1.0d)?this.zoom/2.0d:this.zoom;
		// }
		// System.out.println(this.zoom);
		// RepaintManager.currentManager(this).markCompletelyDirty(this);
	}

	public void createPAIm(final BufferedImage bi) {

		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		final Color c = g2.getColor();
		final Rectangle rect = this.pa.getEnclosingRectangle();

		this.paSize = rect.getBounds().getSize();
		// System.out.println(this.paSize);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		        1.0f));
		g2.setColor(Color.BLACK);
		g2.fill(rect);
		setPreferredSize(new Dimension(getWidth(), getHeight()));
		setMaximumSize(new Dimension(getWidth(), getHeight()));

		final Color[] cls = new Color[] { Color.GREEN, Color.CYAN };
		final Composite comp = g2.getComposite();
		final Composite alpha = AlphaComposite.getInstance(
		        AlphaComposite.SRC_OVER, 0.7f);
		final Shape r = this.pa.getShape();
		EvalTools.notNull(r, this);
		g2.setComposite(alpha);
		g2.setColor(Color.WHITE);
		g2.fill(rect);
		g2.setColor(Color.RED);
		g2.fill(r);
		for (int i = 0; i < this.pa.rows(); i++) {
			for (int j = 0; j < this.pa.columns(); j++) {
				if (this.pa.inRange(i, j)) {
					g2.setColor(cls[(i + j) % cls.length]);
					g2.fillRect(j, i, 1, 1);
				}
			}
		}
		// g2.setColor(Color.BLUE);
		// g2.setComposite(alpha);
		for (final Tuple2D<Integer, Integer> t : this.aps
		        .getCorrespondingScans()) {
			g2.setColor(cls[(t.getFirst() + t.getSecond()) % cls.length]);
			g2.fillRect(t.getSecond(), t.getFirst(), 1, 1);
		}

		g2.dispose();
	}

	public BufferedImage getContent() {
		return this.paIm;
	}

	@Override
	public Dimension getMaximumSize() {
		return super.getMaximumSize();
	}

	@Override
	public Dimension getPreferredSize() {
		return super.getPreferredSize();
	}

	@Override
	protected void paintComponent(final Graphics g) {

		super.paintComponent(g);
		if (this.rr == null) {
			this.rr = new RepaintRunnable();
		}
		this.rr.setGraphics(g);
		// AffineTransform graphicsTransl =
		// AffineTransform.getTranslateInstance(getWidth()/2,getHeight()/2);
		// AffineTransform graphicsScale =
		// AffineTransform.getScaleInstance(zoom, zoom);
		// g2.setTransform(graphicsTransl);
		this.rr.run();
		g.dispose();
	}
}
