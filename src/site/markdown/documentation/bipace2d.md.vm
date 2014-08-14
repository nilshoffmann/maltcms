<h2>BiPACE and BiPACE2D Peak Alignment</h2>
Before continuing, please check that [Maltcms is correctly installed and working](../gettingStarted.html)!

<h3>Converting ChromaTOF peak lists to netCDF format</h3>
- Download the latest release of the [Maltcms User Interface Maui](http://maltcms.sf.net/maui)
- Install the application and create a new project
- Select the ChromaTOF peak reports as your 'Data Files'
- Assign groups to your peak reports
- Set the 'Separation Type' to GCxGC
- Set the 'Detector Type' to TOF-MS (for Leco Pegasus IV data)
- Select 'Override' and set 'Modulation Time' and 'Scan Rate' according to your setup
- Finish the wizard and wait for the import to complete
- The converted files are now below the project's directory, below `import/ChromaTofPeakListImporter` and a time stamp indicating the date and time of the import.

<h3>Setting parameters</h3>
Good starting parameters for BiPACE2D are `D1=stdev(t1)`, `D2=stdev(t2)`, effectively the expected standard deviation in the first and second retention time, and `T1=0.0` and `T2=0.0`, as the retention time matching thresholds to control the number of false positives. Peaks with peak retention time deviations resulting in a function value of the Gaussian retention penalty term (function value ranges between `0` and `1`) below the given threshold (`T1` or `T2`) are removed from further consideration.

The minimum clique size parameter only influences the output of BiPACE2D, so it is a simple filter to select only cliques that match or exceed the given parameter value. As the similarity function, the weighted cosine has proven to be both fast and precise.

BiPACE2D is included in the Maltcms software framework that is available from [the Maltcms Website](http://maltcms.sf.net). After downloading and extracting the Maltcms distribution, the directory structure contains a folder termed `cfg`. Below that folder, the `pipelines/xml` folder contains the definition of various pipelines that can be executed with Maltcms. The folder `pipelines` contains one `.mpl` properties file (maltcms pipe line) for each xml-based pipeline definition in the `xml` folder. The configuration file `bipace2D.xml` contains the configuration information for the parameters used by BiPACE2D.

These include, as direct parameters of the bean tag element with id `peakCliqueAlignment`:

- `MCS=minCliqueSize` - the minimum clique size to be reported in the multiple alignment
- `saveUnmatchedPeaks` - whether peaks without a best hit should be saved to an MSP compatible file 
- `saveUnassignedPeaks` - whether peaks that are not a part of a biclique or bidirectional best hit should be saved to an MSP compatible file
- `saveIncompatiblePeaks` - whether peaks that were not mergeable into larger cliques should be saved to an MSP compatible file

Other parameters are configured in other bean tags, that are referenced by their id in the `peakCliqueAlignment` tag.

Array Similarity (referenced in bean with id `timePenalizedProductSimilarity`), one of:

- `dotSimilarity` - dot product similarity
- `cosineSimilarity` - cosine similarity
- `linCorrSimilarity` - Pearson's linear correlation coefficient
- `weightedCosineSimilarity` - weighted cosine similarity

Retention Time Penalties (referenced in bean with id `timePenalizedProductSimilarity`):

- first rt dimension: bean with id `gaussianDifferenceSimilarityRt1`:	
    - `D1=tolerance` - the retention time tolerance in the first separation dimension
    - `T1=threshold` - the retention time tolerance threshold in the first separation dimension
- second rt dimension: bean with id `gaussianDifferenceSimilarityRt2`:
    - `D2=tolerance` - the retention time tolerance in the second separation dimension
    - `T2=threshold` - the retention time tolerance threshold in the second separation dimension
    
Maximum search range for first and second column retention times (in bean with id `worker2DFactory`):

- `maxRTDifferenceRt1` - maximum retention time difference on first column to include in comparison, usually `=2*D1`
- `maxRTDifferenceRt2` - maximum retention time difference on second column to include in comparison, usually `=2*D2`

<h3>Running BiPACE2D</h3>
Change to the directory where you extracted the downloaded Maltcms version and run:

    > bin/maltcms.sh -f path/to/converted/files/*.cdf -c cfg/pipelines/chroma2D.mpl

The program will start up and print its progress to the terminal. When it is finished, it will have created a folder `maltcmsOutput` below which the results are stored.

