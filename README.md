# The Modular Application Toolkit for Chromatography-Mass Spectrometry
[![Java CI with Maven](https://github.com/nilshoffmann/maltcms/actions/workflows/maven.yml/badge.svg)](https://github.com/nilshoffmann/maltcms/actions/workflows/maven.yml)
[![Release Version](https://img.shields.io/github/release/nilshoffmann/maltcms.svg)](https://github.com/nilshoffmann/maltcms/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/nilshoffmann/maltcms/total.svg)](https://github.com/nilshoffmann/maltcms/releases/latest)
[![License](https://img.shields.io/badge/license-LGPL--3.0-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
[![License](https://img.shields.io/badge/license-EPL--1.0-blue.svg)](https://www.eclipse.org/legal/epl-v10.html)

[:blue_book: Documentation](docs/index.md)

Maltcms is a JAVA framework for preprocessing, alignment, analysis and visualization of data stored in open file formats used in proteomics, metabolomics, and analytical chemistry research.

Maltcms is implemented in the platform-independent JAVA programming language. The individual modules of Maltcms are managed and built using Maven.

If you want to use Maltcms in your own projects, please see the [Getting Started page](docs/gettingStarted.md) for more details.

Maltcms is dual licensed under either the Lesser General Public License (L-GPL v3), or, at the licensees discretion, under the terms of the Eclipse Public License (EPL v1).

## Features

### Open Data Formats

* supports chromatography and mass-spectral data from netcdf (ANDI-MS/-Chrom), mzXML, mzData and mzML formats
* supports msp-format (Amdis, NIST MS Search, GMD-DB) database import for mass spectral search
* mass-spectral data export in msp format
* comma-separated-value (csv) data export for table-like data, e.g. alignments, peak tables etc.

### Software Framework

* generic framework for custom processing of chromatography-mass spectrometry data
* efficient access to files of arbitrary size
* easy to use level-of-abstraction application programming interface
* creation of charts and plots for chromatograms, mass spectra, aligned and unaligned
* academic and business friendly licensing (L-GPL v3 or EPL)

### Parallelization

* individual tasks can be executed concurrently
* Mpaxs provides transparent parallel execution within the same virtual machine or within a distributed grid environment
* convenience utilties for executing and collection of results from embarrassingly parallel tasks
* simple integration with JAVAs Runnable and Callable classes

### ChromA

* fast peak finder for chromatographic peaks
* import of significant peaks as alignment anchors
* multiple alignment and peak matching based on mass spectral similarities
* clustering of chromatograms based on alignments

### ChromA4D

* peak finder for comprehensive GCxGC-MS and other multicolumn chromatography-MS
* peak area integration based on 2D-TIC
* multiple alignment and peak matching based on mass spectral similarities

### Analytical Pyrolysis

* peak finder for comprehensive GCxGC-MS and other multicolumn chromatography-MS
* peak area integration based on 2D-TIC
* multiple alignment and peak matching based on mass spectral similarities

## Prerequisites for Maltcms Usage
* Java JDK / JRE 17 https://www.oracle.com/technetwork/java/javase/downloads/index.html

## Prerequisites for Maltcms Development

* [Git](https://git-scm.com) (for source code access)
* [Java JDK 17](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven 3](https://maven.apache.org/download.html)
* Any IDE supporting maven 3 integration and subversion support
    * [NetBeans 8.+](https://netbeans.org/)
    * [Eclipse 4.+](https://eclipse.org/)
    (requires additional m2e plugin and 
     git plugins)
    * [IntelliJ Idea 10.5](https://www.jetbrains.com/idea/)

Within this directory, you will find all the submodules, which 
make up Maltcms. General configuration for all modules can be found within the 
file pom.xml in the same directory as this README. Module-specific
configuration can be found below each module's directory in the 
corresponding pom.xml file. 

## References

* [Kuich, P.H.J.L., Hoffmann, N. and Kempa, S. (2014) **Maui-VIA: A User-Friendly Software for Visual Identification, Alignment, Correction, and Quantification of Gas Chromatography–Mass Spectrometry Data**, Frontiers in Bioengineering and Biotechnology, 2, p. 289.](https://doi.org/10.3389/fbioe.2014.00084)
* [Hoffmann, N. (2014) **Computational methods for high-throughput metabolomics**, Dissertation, PUB Bielefeld University](https://pub.uni-bielefeld.de/record/2677466)
* [Hoffmann, N. et al. (2014) **BiPACE 2D—graph-based multiple alignment for comprehensive 2D gas chromatography-mass spectrometry**, Bioinformatics, 30(7), pp. 988–995.](https://doi.org/10.1093/bioinformatics/btt738)
* [Hoffmann, N. et al. (2012) **Combining peak- and chromatogram-based retention time alignment algorithms for multiple chromatography-mass spectrometry datasets**, BMC Bioinformatics, 13(1), p. 214.](https://doi.org/10.1186/1471-2105-13-214)
* [Hoffmann, N. and Stoye, J. (2012) **Generic Software Frameworks for GC-MS Based Metabolomics**, in U. Roessner (ed.) Metabolomics. InTech.](https://doi.org/10.5772/31224)
* [Hoffmann, N. and Stoye, J. (2009) **ChromA: signal-based retention time alignment for chromatography–mass spectrometry data**, Bioinformatics, 25(16), pp. 2080–2081.](https://doi.org/10.1093/bioinformatics/btp343)
