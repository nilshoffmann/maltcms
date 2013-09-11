/* 
 * Maltcms, modular application toolkit for chromatography-mass spectrometry. 
 * Copyright (C) 2008-2012, The authors of Maltcms. All rights reserved.
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
package maltcms.datastructures.peak;

import java.awt.Point;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import maltcms.io.xml.bindings.annotation.AnnotationType;
import maltcms.io.xml.bindings.annotation.AnnotationsType;
import maltcms.io.xml.bindings.annotation.AttributeType;
import maltcms.io.xml.bindings.annotation.MaltcmsAnnotation;
import maltcms.io.xml.bindings.annotation.ResourceType;

/**
 * @author Nils Hoffmann
 *
 *
 */
public class MaltcmsAnnotationFactory {

    public MaltcmsAnnotation load(File f) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("maltcms.io.xml.bindings.annotation");
            final Unmarshaller u = jc.createUnmarshaller();
            final MaltcmsAnnotation mzd = (MaltcmsAnnotation) u.unmarshal(f);
            return mzd;
        } catch (final JAXBException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public void save(MaltcmsAnnotation mat, File f) {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("maltcms.io.xml.bindings.annotation");
            final Marshaller u = jc.createMarshaller();
            u.marshal(mat, f);
        } catch (final JAXBException e) {
            throw new RuntimeException(e.fillInStackTrace());
        }
    }

    public MaltcmsAnnotation createNewMaltcmsAnnotationType(URI resource) {
        MaltcmsAnnotation ma = new MaltcmsAnnotation();
        ResourceType rt = new ResourceType();
        rt.setUri(resource.toString());
        ma.setResource(rt);
        return ma;
    }

    public AnnotationsType getAnnotationsTypeFor(MaltcmsAnnotation ma,
            String creator, Object annotationObject) {
        List<AnnotationsType> l = ma.getAnnotations();
        AnnotationsType mat = null;
        for (AnnotationsType at : l) {
            if (at.getType().equals(annotationObject.getClass().getName())
                    && at.getGenerator().equals(creator)) {
                mat = at;
            }
        }
        if (mat == null) {
            mat = new AnnotationsType();
            mat.setGenerator(creator);
            mat.setType(annotationObject.getClass().getName());
            l.add(mat);
        }
        return mat;
    }

    public void addPeakAnnotation(MaltcmsAnnotation ma, String creator, IPeak p) {
        AnnotationsType peaks = getAnnotationsTypeFor(ma, creator, p);
        AnnotationType at = new AnnotationType();
        at.setType(p.getClass().getName());
        at.setName(p.getName());
        AttributeType si = new AttributeType();
        si.setName("scan_index");
        si.setValue(p.getScanIndex() + "");
        addIfNew(si, at);

        addIfNew(at, peaks);
    }

    public void addIfNew(AnnotationType anot, AnnotationsType anost) {
        if (!anost.getAnnotation().contains(anot)) {
            anost.getAnnotation().add(anot);
        }
    }

    public void addIfNew(AttributeType at, AnnotationType annt) {
        if (!annt.getAttribute().contains(at)) {
            annt.getAttribute().add(at);
        }
    }

    public List<Peak1D> getPeak1D(MaltcmsAnnotation ma) {
        List<Peak1D> peaks = new ArrayList<Peak1D>();
        for (AnnotationsType ann : ma.getAnnotations()) {
            String type = ann.getType();
            if (type.equals(Peak1D.class.getName())) {
                Peak1D p = new Peak1D();
                List<AnnotationType> atl = ann.getAnnotation();
                if (!atl.isEmpty()) {
                    if (atl.size() != 1) {
                        throw new IllegalArgumentException(
                                "Peak annotation contained multiple annotation lists!");
                    }
                    AnnotationType annt = atl.get(0);
                    for (AttributeType at : annt.getAttribute()) {
                        if (at.getName().equals("name")) {
                            p.setName(at.getValue());
                        } else if (at.getName().equals("scan_index")) {
                            p.setApexIndex(Integer.parseInt(at.getValue()));
                        } else if (at.getName().equals("apex_index")) {
                            p.setApexIndex(Integer.parseInt(at.getValue()));
                        } else if (at.getName().equals("start_index")) {
                            p.setStartIndex(Integer.parseInt(at.getValue()));
                        } else if (at.getName().equals("stop_index")) {
                            p.setStopIndex(Integer.parseInt(at.getValue()));
                        } else if (at.getName().equals("apex_time")) {
                            p.setApexTime(Double.parseDouble(at.getValue()));
                        } else if (at.getName().equals("start_time")) {
                            p.setStartTime(Double.parseDouble(at.getValue()));
                        } else if (at.getName().equals("stop_time")) {
                            p.setStopTime(Double.parseDouble(at.getValue()));
                        } else if (at.getName().equals("area")) {
                            p.setArea(Double.parseDouble(at.getValue()));
                        } else if (at.getName().equals("intensity")) {
                            p.setApexIntensity(Double.parseDouble(at.getValue()));
                        } else if (at.getName().equals("mw")) {
                            p.setMw(Double.parseDouble(at.getValue()));
                        }
                        // TODO add support for peak area
                    }
                    peaks.add(p);
                }
            }
        }
        return peaks;
    }

    public void addPeakAnnotation(MaltcmsAnnotation ma, String creator, Peak1D p) {
        AnnotationsType peaks = getAnnotationsTypeFor(ma, creator, p);

        AnnotationType at = new AnnotationType();
        at.setType(p.getClass().getName());
        at.setName(p.getName());

        AttributeType si = new AttributeType();
        si.setName("name");
        si.setValue(p.getName() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("apex_index");
        si.setValue(p.getApexIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("start_index");
        si.setValue(p.getStartIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("stop_index");
        si.setValue(p.getStopIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("apex_time");
        si.setValue(p.getApexTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("start_time");
        si.setValue(p.getStartTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("stop_time");
        si.setValue(p.getStopTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("area");
        si.setValue(p.getArea() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("intensity");
        si.setValue(p.getApexIntensity() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("mw");
        si.setValue(p.getMw() + "");
        addIfNew(si, at);

        addIfNew(at, peaks);
    }

    public Peak2D getPeakAnnotation(AnnotationType peak) {
        Peak2D p = new Peak2D();
        for (AttributeType at : peak.getAttribute()) {
            if (at.getName().equals("name")) {
                p.setName(at.getValue());
            } else if (at.getName().equals("scan_index")) {
                p.setScanIndex(Integer.parseInt(at.getValue()));
            } else if (at.getName().equals("apex_index")) {
                p.setApexIndex(Integer.parseInt(at.getValue()));
            } else if (at.getName().equals("start_index")) {
                p.setStartIndex(Integer.parseInt(at.getValue()));
            } else if (at.getName().equals("stop_index")) {
                p.setStopIndex(Integer.parseInt(at.getValue()));
            } else if (at.getName().equals("apex_time")) {
                p.setApexTime(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("start_time")) {
                p.setStartTime(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("stop_time")) {
                p.setStopTime(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("area")) {
                p.setArea(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("intensity")) {
                p.setApexIntensity(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("mw")) {
                p.setMw(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("first_column_retention_time")) {
                p.setFirstRetTime(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("second_column_retention_time")) {
                p.setSecondRetTime(Double.parseDouble(at.getValue()));
            } else if (at.getName().equals("index")) {
                p.setIndex(Integer.parseInt(at.getValue()));
            } else if (at.getName().equals("similarity")) {
                // FIXME Peak2D should not support this directly
            } else if (at.getName().equals("names")) {
                // FIXME Peak2D should not support this directly
            }
            // TODO add support for peak area
        }
        return p;
    }

    public void addPeakAnnotation(MaltcmsAnnotation ma, String creator, Peak2D p) {
        AnnotationsType peaks = getAnnotationsTypeFor(ma, creator, p);

        AnnotationType at = new AnnotationType();
        at.setType(p.getClass().getName());
        at.setName(p.getName());

        AttributeType si = new AttributeType();
        si.setName("name");
        si.setValue(p.getName() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("scan_index");
        si.setValue(p.getScanIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("apex_index");
        si.setValue(p.getApexIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("start_index");
        si.setValue(p.getStartIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("stop_index");
        si.setValue(p.getStopIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("apex_time");
        si.setValue(p.getApexTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("start_time");
        si.setValue(p.getStartTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("stop_time");
        si.setValue(p.getStopTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("area");
        si.setValue(p.getArea() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("intensity");
        si.setValue(p.getApexIntensity() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("mw");
        si.setValue(p.getMw() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("first_column_retention_time");
        si.setValue(p.getFirstRetTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("second_column_retention_time");
        si.setValue(p.getSecondRetTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("index");
        si.setValue(p.getIndex() + "");
        addIfNew(si, at);

        si = new AttributeType();
        si.setName("retention_time");
        si.setValue(p.getRetentionTime() + "");
        si.setUnit("seconds");
        addIfNew(si, at);

        // si = new AttributeType();
        // si.setName("similarity");
        // si.setValue(p.getSim() + "");
        // addIfNew(si, at);
        //
        // si = new AttributeType();
        // si.setName("names");
        // if (p.getNames().isEmpty()) {
        // si.setValue("");
        // } else {
        // List<Tuple2D<Double, IMetabolite>> names = p.getNames();
        // StringBuilder sb = new StringBuilder();
        // for (Tuple2D<Double, IMetabolite> t : names) {
        // sb.append("[" + t.getFirst() + ":" + t.getSecond().getID()
        // + "],");
        // }
        // sb.replace(sb.length() - 1, sb.length(), "\n");
        // si.setValue(sb.toString());
        // }
        // addIfNew(si, at);

        PeakArea2D pa = p.getPeakArea();
        if (pa != null) {
            StringBuilder sb = new StringBuilder();
            si = new AttributeType();
            si.setName("area2DSeed");
            si.setValue(pa.getSeedPoint().x + " " + pa.getSeedPoint().y);
            addIfNew(si, at);

            si = new AttributeType();
            si.setName("area2DboundaryPoints");
            List<Point> bps = pa.getBoundaryPoints();

            sb = new StringBuilder();
            if (!bps.isEmpty()) {
                for (Point pt : bps) {
                    sb.append("[" + pt.x + ":" + pt.y + "],");
                }
                sb.replace(sb.length() - 1, sb.length(), "\n");
            }
            si.setValue(sb.toString());
            addIfNew(si, at);
        }

        addIfNew(at, peaks);
    }
}
