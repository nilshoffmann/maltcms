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
package maltcms.datastructures.alignment;

import cross.datastructures.fragments.IFileFragment;
import cross.exception.ResourceNotAvailableException;
import cross.tools.StringTools;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import maltcms.io.xml.bindings.alignment.Alignment;
import maltcms.io.xml.bindings.alignment.MappedPointsType;
import maltcms.io.xml.bindings.alignment.PointMapType;
import maltcms.io.xml.bindings.alignment.PointType;
import maltcms.io.xml.bindings.alignment.PointType.Dimension;
import maltcms.io.xml.bindings.alignment.ResourceType;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class AlignmentFactory {

    public Alignment load(File f) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("maltcms.io.xml.bindings.alignment");
            final Unmarshaller u = jc.createUnmarshaller();
            final Alignment mzd = (Alignment) u.unmarshal(f);
            return mzd;
        } catch (final JAXBException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public void save(Alignment mat, File f) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("maltcms.io.xml.bindings.alignment");
            final Marshaller u = jc.createMarshaller();
            u.marshal(mat, f);
        } catch (final JAXBException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public Alignment createNewAlignment(String creator, boolean isCompleteMap) {
        Alignment ma = new Alignment();
        ma.setGenerator(creator);
        ma.setIsCompleteMap(isCompleteMap);
        return ma;
    }

    public MappedPointsType getMappedPoints(Alignment a, IFileFragment f)
            throws ResourceNotAvailableException {
        List<MappedPointsType> l = a.getMappedPoints();
        for (MappedPointsType mpt : l) {
            String uri = mpt.getResource().getUri();
            String clippedUri = StringTools.removeFileExt(uri);
            String fname = StringTools.removeFileExt(f.getName());
            if (clippedUri.endsWith(fname)) {
                return mpt;
            }
        }
        throw new ResourceNotAvailableException(
                "Could not find a mappedPoints entry for "
                + f.getUri());
    }

    public List<Integer> convertToScanIndexMap(MappedPointsType mpt)
            throws IllegalArgumentException {
        PointMapType pmt = mpt.getPointMap();
        List<PointType> points = pmt.getPoint();
        List<Integer> list = new ArrayList<>();
        for (PointType pt : points) {
            Dimension d;
            try {
                d = getDimensionByName("scan_index", pt);
                list.add(Integer.parseInt(d.getValue()));
            } catch (ResourceNotAvailableException rnae) {
                throw new IllegalArgumentException(
                        "Could not find required Dimension with name scan_index below PointType.",
                        rnae);
            }
        }
        return list;
    }

    public Dimension getDimensionByName(String name, PointType pt)
            throws ResourceNotAvailableException {
        List<Dimension> l = pt.getDimension();
        for (Dimension d : l) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        throw new ResourceNotAvailableException(
                "Could not find Dimension with name " + name
                + " for PointType!");
    }

    public void addScanIndexMap(Alignment a, URI resource, List<Integer> l,
            boolean isAlignmentReference) {
        List<MappedPointsType> mp = a.getMappedPoints();

        MappedPointsType mpt = new MappedPointsType();
        ResourceType rt = new ResourceType();
        rt.setUri(resource.toString());
        mpt.setResource(rt);
        mpt.setIsAlignmentReference(isAlignmentReference);
        PointMapType pmt = new PointMapType();
        pmt.setDimNumber(BigInteger.ONE);
        pmt.setPointNumber(BigInteger.valueOf(l.size()));
        mpt.setPointMap(pmt);
        List<PointType> points = pmt.getPoint();
        points.clear();
        for (Integer itg : l) {
            PointType pt = new PointType();
            Dimension d = new Dimension();
            d.setName("scan_index");
            d.setValue(Integer.toString(itg));
            pt.getDimension().add(d);
            points.add(pt);
        }
        mp.add(mpt);
        a.setNumberOfMaps(BigInteger.valueOf(mp.size()));
    }
}
