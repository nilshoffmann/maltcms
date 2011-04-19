/*
 * Copyright (C) 2009, 2010 Mathias Wilhelm mwilhelm A T
 * TechFak.Uni-Bielefeld.DE
 * 
 * This file is part of Cross/Maltcms.
 * 
 * Cross/Maltcms is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Cross/Maltcms is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Cross/Maltcms. If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */
package maltcms.commands.distances;

import org.apache.commons.configuration.Configuration;

import ucar.ma2.Array;
import ucar.ma2.MAVector;
import cross.Logging;
import cross.annotations.Configurable;

/**
 * Implementation of Tanimoto score.
 * 
 * @author Mathias Wilhelm(mwilhelm A T TechFak.Uni-Bielefeld.DE)
 */
public class ArrayTimePenalizedTanimoto implements IArrayDoubleComp {

    private IArrayDoubleComp sf = new ArrayTanimoto();

    @Configurable(name = "expansion_weight")
    private double wExp = 1.0d;
    @Configurable(name = "compression_weight")
    private double wComp = 1.0d;
    @Configurable(name = "diagonal_weight")
    private double wDiag = 1.0d;
    @Configurable
    private double rtTolerance = 50.0d;
    @Configurable
    private double rtEpsilon = 0.01d;

    /**
     * {@inheritDoc}
     */
    @Override
    public Double apply(final int i1, final int i2, final double time1,
            final double time2, final Array t1, final Array t2) {

        final double weight = ((time1 == -1) || (time2 == -1)) ? 1.0d
                : Math.exp(-((time1 - time2) * (time1 - time2) / (2.0d * this.rtTolerance * this.rtTolerance)));
        // 1 for perfect time correspondence, 0 for really bad time
        // correspondence (towards infinity)
        if (weight - this.rtEpsilon < 0) {
            return Double.NEGATIVE_INFINITY;
        }

        return weight*sf.apply(i1, i2, time1, time2, t1, t2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCompressionWeight() {
        return wComp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDiagonalWeight() {
        return wDiag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getExpansionWeight() {
        return wExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean minimize() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Configuration cfg) {
        this.rtTolerance = cfg.getDouble(this.getClass().getName()
                + ".rtTolerance", 50.0d);
        this.rtEpsilon = cfg.getDouble(
                this.getClass().getName() + ".rtEpsilon", 0.01d);
        this.wComp = cfg.getDouble(this.getClass().getName()
                + ".compression_weight", 1.0d);
        this.wExp = cfg.getDouble(this.getClass().getName()
                + ".expansion_weight", 1.0d);
        this.wDiag = cfg.getDouble(this.getClass().getName()
                + ".diagonal_weight", 2.25d);
        StringBuilder sb = new StringBuilder();
        sb.append("wComp: " + this.wComp + ", ");
        sb.append("wExp: " + this.wExp + ", ");
        sb.append("wDiag: " + this.wDiag);
        Logging.getLogger(this).info("Parameters of class {}: {}",
                this.getClass().getName(), sb.toString());
    }
}
