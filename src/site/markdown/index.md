## Welcome to Maltcms
Maltcms is a JAVA framework for preprocessing, alignment, analysis and visualization of data stored in open file formats used in proteomics, metabolomics, and analytical chemistry research.

Maltcms is implemented in the platform-independent JAVA programming language. 
The individual modules of Maltcms are managed and built using [Maven](http://maven.apache.org).

If you want to use Maltcms in your own projects, please see the <a href="gettingStarted.html">Getting Started</a> page for more details.

Maltcms is <a href="http://sf.net/p/maltcms/info/license.html">dual licensed</a> under either the Lesser General Public License (L-GPL v3), or, at the licensees discretion, under the terms of the Eclipse Public License (EPL v1).

---

## Open Data Formats
* supports chromatography and mass-spectral data from netcdf (ANDI-MS/-Chrom), mzXML, mzData and mzML formats
* supports msp-format (<a alt="Amdis" href="http://chemdata.nist.gov/mass-spc/amdis/">Amdis</a>, <a alt="NIST MS Search" href="http://chemdata.nist.gov/mass-spc/ms-search/">NIST MS Search</a>, <a alt="Golm Metabolome Database" href="http://gmd.mpimp-golm.mpg.de/">GMD-DB</a>) database import for mass spectral search
* mass-spectral data export in msp format
* comma-separated-value (csv) data export for table-like data, e.g. alignments, peak tables etc.

## Software Framework
* generic framework for custom processing of chromatography-mass spectrometry data
* efficient access to files of arbitrary size
* easy to use level-of-abstraction application programming interface
* creation of charts and plots for chromatograms, mass spectra, aligned and unaligned
* academic and business friendly licensing (<a href="http://sf.net/p/maltcms/info/license.html" title="License">L-GPL v3 or EPL</a>)

## Parallelization
* individual tasks can be executed concurrently
* <a alt="Modular parallelization and execution system" href="http://sf.net/p/mpaxs/">Mpaxs</a> provides transparent parallel execution within the same virtual machine or within a distributed grid environment
* convenience utilties for executing and collection of results from embarrassingly parallel tasks
* simple integration with JAVAs `Runnable` and `Callable` classes

## ChromA
* fast peak finder for chromatographic peaks
* import of significant peaks as alignment anchors
* multiple alignment and peak matching based on mass spectral similarities
* clustering of chromatograms based on alignments

## ChromA4D
* peak finder for comcodehensive GCxGC-MS and other multicolumn chromatography-MS
* peak area integration based on 2D-TIC
* multiple alignment and peak matching based on mass spectral similarities

## Analytical Pyrolysis
* peak finder for comcodehensive GCxGC-MS and other multicolumn chromatography-MS
* peak area integration based on 2D-TIC
* multiple alignment and peak matching based on mass spectral similarities

