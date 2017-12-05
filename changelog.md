- Added jaxb-core dependencies, updaged versions.

- Restructured distributions, osgi wrappers, and io providers into separate submodules. Updated versions.

- Updated JFreechart version.

- Fixed failing test by correcting workflow setup and default variable loading. Corrected imports. Fixed distribution assembly build error.

- Fixed failing test by correcting workflow setup and default variable loading. Corrected imports. Restructured distribution documentation.

- Fixed failing test by correcting workflow setup and variable creation.

- Fixed failing test by adding netcdf io provider as test dependency.

- Fixed build failures in Eclipse.

- Refactored integration tests to end with IT suffix. Updated pom configurations to remove warnings and errors in Eclipse.

- Fixed null pointer exception when cpnfiguration from classpath is unavailable.

- Fixed failing test by adding default similarities.

- Updated groovy lib version and compiler version. Updated hppc version. Removed obsolete files.

- Updated failing assertion in integration tests to print an error message. Updated jmztab version to 3.0.2.

- Fixed build issues with Java 8

- Fixed jfreechart packaging. Added expected exception to test.

- Simplified ChromaTOF importer and parser APIs. Updated site documentation.

- Added additional test file for unmapped columns.

- Changed behaviour of ChromaTOFParser to warn about unmapped columns in debug logging level.

- Configuration is now also printed, if no additional -c flag is given.

- Changed Pipelines using BiPACE2D to use ArrayWeightedCos2 as similarity function. Gzipped gcimage test file.

- Updated tests. Fixed a peak initialization bug in AbstractCwtPeakFinderCallable.

- Added site for jmzML OSGI.

- Fixed resource access problem in test case.

- Fixed failing tests for ChromaTOFDataSource.

- Fixed resource not found issues in tests.

- Fixed issues with SparseScanLineCache. Readded Peak2DClique. Updated ChromaTOFPeakListEntityTable to use ChromaTOFParser.ColumnName.

- Updated Peak1D and Peak2D to provide builder methods, simplifying creation. Refactored other classes accordingly. Provided improved support for ChromaTOF parsing.
