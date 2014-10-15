# Release Notes

## Changes for version 1.4.0
### Improvements
* Added support for import of GcImage Blob Reports in csv format as peak data in `maltcms.commands.fragments2d.io.csv.GcImagePeak2DImporter`.
* Fixed an issue in `MZMLExporterWorker` where validation would fail, if file names started with numbers.
* Added support in `Peak2D` for reading and writing of peak lists from and to netcdf. 
* Refactored and simplified `SeededRegionGrowing` to primarily use the `IChromatogram2D` interface.

### API Changes

* Removed extra steps in `SeededRegionGrowing`, thereby removing a number of configuration options. Alignment can now be performed by `PeakCliqueAlignment`. Example pipelines have been adapted accordingly.
* Peak finding classes have been refactored to share large amounts of similar code, this may require adaptation of the pipeline configurations.
* Extended IChromatogram2D interface to allow retrieval of 2D time bound region.

### Dependencies

* Updated cross dependency to latest stable 1.4.0 release. 

## Changes for version 1.3.1
### Improvements

* Added additional option to reuse exising pipeline results, if input and parameters of preceding fragment commands have not changed.
* Modified commands and similarities to behave equals/hashCode friendly.
* Fixed issues with images of plots not being saved to the correct file.
* Increased default size of plots.
* Updates to GCGC-MS peakfinding and integration.
* Updated AMDISMSSimilarity to use WeightedCos as similarity.
* Modified PeakCliqueAlignment to accept peak list information from SRG.
* Fixed PeakCliqueAlignment issue not respecting minCliqueSize. 
* Updated PeakCliqueAlignment / BiPACE to perform better for large input sizes.
* Parallelized TICPeakFinder.
* Added missing maltcms.home variable in maltcms.bat. 
* Updated ehcache configuration.
* Added new pipeline for scan extraction and andims export. 
* Fixed issues with XML-based io providers.

### API Changes

* Extended IScan interface with precursor_mz, precursor_charge, and precursor_intensity. 
* Multiple updates and refactoring of IScanLine API. 
* Added interface ICopyable to filters.
* The class maltcms.tools.PublicMemberGetters has been moved to the cross project at cross.tools.PublicMemberGetters.
* cross.Factory.getInstance() now returns cross.IFactory(). The static singleton access pattern has been deprecated and will be replaced in the next major release version. Refactored static method access wrt `cross.Factory`.

### Data Format support

* Added validation parameter to MZMLExporter. Added MZMLValidator class for schema-based validation.
* Improved error handling and compatibility of MZMLExporterWorker and DataSource.
* Fixed open file issues in mzXML provider.
* Added additional bounds checks in NetcdfDataSource to avoid invalid scan_index values to crash io operations.

### Dependencies

* Updated lombok dependency to latest stable 1.12.4 release. 
 
This version is backwards compatible to 1.3.x releases of maltcms concerning the workflow format. However, some commands have additional properties, while some commands have fewer properties, so please check whether your pipelines require updating. Parameters are introduced with sensible default values, usually meaning that they are not used when not explicitly configured. Go [here](./documentation/fragmentCommands.html) for more information on how to inquire a fragment command for its supported parameters.

## Changes for version 1.3.0

### Improvements

* Improved [BiPACE and BiPACE2D](./documentation/bipace2d.html) pipelines for alignment of peak lists from 1D and 2D chromatography-mass spectrometry.
* Maltcms artifacts now expose osgi dependency information and can be deployed 
in osgi-compatible runtime containers. The maltcms-osgi-distribution module lists all required 
dependencies.
* The maltcms-nbm module provides NetBeans Rich Client Platform compatible modules.
* Preliminary support for Agilent Chemstation _.D_ directories with peak reports in '.xls' format.
* MS/N Support for mzML, also available for custom NetCDF format via ms_level variable.
* Preliminary Support for extraction of mzML TIC chromatograms via total_ion_current_chromatogram and total_ion_current_chromatogram_scan_acquisition_time.
* New optional caching system for lower memory footprint using Ehcache.

This version is backwards compatible to 1.2.x releases of maltcms concerning the workflow format. However, some commands have additional properties, so please check whether your pipelines require updating. Parameters are introduced with sensible default values, usually meaning that they are not used when not explicitly configured. Go [here](./documentation/fragmentCommands.html) for more information on how to inquire a fragment command for its supported parameters. 

