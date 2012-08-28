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
package maltcms.io.xml.mzXML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.slf4j.Logger;

import ucar.ma2.Array;
import cross.Factory;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.exception.NotImplementedException;
import cross.exception.ResourceNotAvailableException;
import cross.io.IDataSource;
import cross.tools.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Nils.Hoffmann@CeBiTec.Uni-Bielefeld.DE
 *
 *
 */
@Slf4j
@ServiceProvider(service = IDataSource.class)
public class MZXMLDataSource implements IDataSource {

    private final List<IDataSource> ds = new ArrayList<IDataSource>();
    private final WeakHashMap<IFileFragment, IDataSource> fragmentToValidReaderMap = new WeakHashMap<IFileFragment, IDataSource>();

    public MZXMLDataSource() {
        final List<String> dss = Arrays.asList(
                "maltcms.io.xml.mzXML.MZXMLStaxDataSource",
                "maltcms.io.xml.mzXML.MZXMLSaxDataSource");
        for (final String s : dss) {
            this.ds.add(Factory.getInstance().getObjectFactory().instantiate(s,
                    cross.io.IDataSource.class));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.io.IDataSource#canRead(cross.datastructures.fragments.IFileFragment
     * )
     */
    @Override
    public int canRead(final IFileFragment ff) {

        // try {
        // XMLReader reader = XMLReaderFactory.createXMLReader();
        // reader.setContentHandler(new DefaultHandler() {
        // public void startElement(String uri, String localName,
        // String qName, Attributes attributes)
        // throws SAXException {
        // // System.out.println(qName);
        // }
        // });
        // reader.parse(new InputSource(new FileInputStream(ff
        // .getAbsolutePath())));
        //
        // } catch (SAXException e) {
        // e.printStackTrace();
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        try {
            final IDataSource ids = getValidReader(ff);
            return 1;
        } catch (final NotImplementedException nie) {
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.commons.configuration.event.ConfigurationListener#
     * configurationChanged
     * (org.apache.commons.configuration.event.ConfigurationEvent)
     */
    @Override
    public void configurationChanged(final ConfigurationEvent arg0) {
        configure(Factory.getInstance().getConfiguration());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.io.IDataSource#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(final Configuration configuration) {
        final List<String> dss = StringTools.toStringList(configuration.getList(getClass().
                getName() + ".dataSources", Arrays.asList(
                "maltcms.io.xml.mzXML.MZXMLStaxDataSource",
                "maltcms.io.xml.mzXML.MZXMLSaxDataSource")));
        for (final String s : dss) {
            this.ds.add(Factory.getInstance().getObjectFactory().instantiate(s,
                    cross.io.IDataSource.class));
        }

    }

    private IDataSource getValidReader(final IFileFragment ff) {
        if (this.fragmentToValidReaderMap.containsKey(ff)) {
            return this.fragmentToValidReaderMap.get(ff);
        }
        for (final IDataSource ids : this.ds) {
            try {
                this.log.info("Checking DataSource {}", ids.getClass().getName());
                if (ids.canRead(ff) > 0) {
                    IDataSource dataSource = Factory.getInstance().getObjectFactory().instantiate(ids.getClass().getName(),
                            cross.io.IDataSource.class);
                    this.fragmentToValidReaderMap.put(ff, dataSource);
                    return dataSource;
                }
            } catch (final RuntimeException e) {
            }
        }
        throw new NotImplementedException("No provider available for "
                + StringTools.getFileExtension(ff.getAbsolutePath()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.io.IDataSource#readAll(cross.datastructures.fragments.IFileFragment
     * )
     */
    @Override
    public ArrayList<Array> readAll(final IFileFragment f) throws IOException,
            ResourceNotAvailableException {
        return getValidReader(f).readAll(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecross.io.IDataSource#readIndexed(cross.datastructures.fragments.
     * IVariableFragment)
     */
    @Override
    public ArrayList<Array> readIndexed(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        return getValidReader(f.getParent()).readIndexed(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecross.io.IDataSource#readSingle(cross.datastructures.fragments.
     * IVariableFragment)
     */
    @Override
    public Array readSingle(final IVariableFragment f) throws IOException,
            ResourceNotAvailableException {
        return getValidReader(f.getParent()).readSingle(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
     * IFileFragment)
     */
    @Override
    public ArrayList<IVariableFragment> readStructure(final IFileFragment f)
            throws IOException {
        return getValidReader(f).readStructure(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecross.io.IDataSource#readStructure(cross.datastructures.fragments.
     * IVariableFragment)
     */
    @Override
    public IVariableFragment readStructure(final IVariableFragment f)
            throws IOException, ResourceNotAvailableException {
        return getValidReader(f.getParent()).readStructure(f);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cross.io.IDataSource#supportedFormats()
     */
    @Override
    public List<String> supportedFormats() {
        final Set<String> al = new HashSet<String>();
        for (final IDataSource ids : this.ds) {
            al.addAll(ids.supportedFormats());
        }
        return new ArrayList<String>(al);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * cross.io.IDataSource#write(cross.datastructures.fragments.IFileFragment)
     */
    @Override
    public boolean write(final IFileFragment f) {
        return getValidReader(f).write(f);
    }
}
