<h2>ChromaTOF<sup>1</sup> Peak Report Support</h2>
Support for reading of ChromaTOF reports is two-fold in Maltcms. 
On a lower level, the *ChromaTOFParser* provides direct access to the 
comma-separated or tab-separated data file in a simple table oriented format:

    //set the locale for correct number parsing
    Locale locale = Locale.US;

    //create an instance of the ChromaTOFParser for a given file, 
    //use the given locale
    ChromaTOFParser parser = ChromaTOFParser.create(file, locale);

    //retrieve the header
    //instruct the parser to use ',' as the field separator (csv mode), 
    //with '"' double quotes fencing strings with spaces of commas
    Set<TableColumn> columnNames = parser.parseHeader(file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);

    //parse the body with the same settings and the given header
    List<TableRow> records = parser.parseBody(columnNames, file, true, ChromaTOFParser.FIELD_SEPARATOR_COMMA, ChromaTOFParser.QUOTATION_CHARACTER_DOUBLETICK);

    //access a specific record (indexing starts at 0)
    TableRow tr2 = records.get(2);

    //the parser will automatically switch to the correct mode, if it detects 
    //either fused 2D rt data in the 'R.T. (s)' column, or two separate fields 
    //'1st Dimension Time (s)' and '2nd Dimension Time (s)'
    Assert.assertEquals(ChromaTOFParser.Mode.RT_2D_SEPARATE, parser.getMode(records));

The *ChromaTOFParser* will determine the actual file type automatically, if the above utility method is used, based on the file extension.
*.csv* files will be parsed with a ',' as field separator and '"' to fence strings, while 
*.tsv* and *.txt* files will be parsed with a '\t' as field separator and '' to fence strings.

If you want to determine explicitly, with which settings the parser is created, proceed as follows:

    String fieldSeparator = ChromaTOFParser.FIELD_SEPARATOR_TAB;
    String quotationCharacter = ChromaTOFParser.QUOTATION_CHARACTER_NONE;
    Locale locale = Locale.US;
    ChromaTOFParser parser = new ChromaTOFParser(fieldSeparator, quotationCharacter, locale);

<h4>Convenience Methods / Classes</h4>
A higher-level API to read ChromaTOF reports is provided by the *ChromaTOFImporter*, giving you 
direct access to a list of *Peak1D* or *Peak2D* objects:

    Locale locale = Locale.US;
    ChromaTOFImporter.importer = new ChromaTOFImporter(locale);
    List<Peak1D> peaks1D = importer.importPeaks(file);

Or alternatively for 2D peaks:
    
    try {
        List<Peak2D> peaks2D = importer.importPeaks2D(file);
    }catch(IllegalArgumentException iae) {
        //if file does not contain 2D peaks
    }

In order to explicitly create a pseudo-ANDI-MS chromatogram with mass spectra, use the following method:
    
    File importDir = new File("importDir");
    File artificialChromatogram = importer.createArtificialChromatogram(importDir, file, peaks, parser.getMode(peaks));

    
<h4>Using ChromaTOF reports as a FileFragment data source</h4>
It is also possible to use ChromaTOF reports as direct input to Maltcms workflows. This is automatically 
possible via the *ChromaTOFDataSource* implementation. However, this data source, in order to conform with 
the other data sources, provides only a limited amount of information. The initial ChromaTOF report is converted to a more 
efficient netCDF data format, that conforms to the basic ANDI-MS specifications as used throughout Maltcms. 
The data source thus provides the following variables:

* *scan_index* (dim: scan_number): the scan index for each mass spectrum.
* *mass_values* (dim: point_number): the recorded mass values for each mass spectrum.
* *intensity_values* (dim: point_number): the recorded intensity values for each mass spectrum.
* *scan_acquisition_time* (dim: scan_number): the recorded acquisition time of a scan (rt1+rt2 for 2D chrom).
* *total_intensity* (dim: scan_number): the total intensity (area field in ChromaTOF) for each peak/scan.
* *mass_range_min* (dim: scan_number): the minimum recorded mass for each scan.
* *mass_range_max* (dim: scan_number): the maximum recorded mass for each scan.

And specifically for 2D chromatography, two additional variables are provided:

* *first_column_elution_time* (dim: scan_number): recorded elution time (s) on the first separation column.
* *second_column_elution_time* (dim: scan_number): recorded elution time (s) on the second separation column.

<h3>Locale settings</h3>
By default, the ChromaTOF Parser will use a US locale for parsing numbers. This means,
that the decimal separator is a "." instead of the ",". Should you experience weird retention times 
after import, or way off areas, check the locale settings for the *ChromaTOFParser* and *ChromaTOFImporter* classes, if 
you use them programmatically, or configure the *ChromaTOFDataSource* locale setting, by adding the following 
property to the *cfg/io.properties* file in the Maltcms base directory:

    maltcms.io.csv.chromatof.ChromaTOFDataSource.locale=de_DE

Maltcms supports the [default Java locales](http://www.oracle.com/technetwork/java/javase/javase7locales-334809.html).

<h3>1D Chromatography Data</h3>
<h4>Supported Column Names</h4>
* *Name* : Putative peak name.
* *R.T. (s)*: Retention Time in seconds. 
* *Type*: The peak type.
* *UniqueMass*: The peak's unique mass, as determined by ChromaTOF.
* *Quant Masses*: The masses used for area quantification.
* *Quant Mass*: The single mass used for area quantification.
* *Quant S/N*: The Signal-to-Noise ratio of the quantification masses' signals.
* *Concentration*: The concentration of the peak.
* *Sample Concentration*: The sample concentration.
* *Match*: The match of the peak.
* *Area*: The quantified, integrated area.
* *Formula*: The putative sum formula.
* *CAS*: The chemical abstracts service number of the putative identification.
* *Similarity*: The match similarity (0-999) of the putative identification.
* *Reverse*: The reverse match similarity (0-999).
* *Probability*: The probability of putative peak assignment.
* *Purity*: The sample purity.
* *Concerns*: Free form concerns about a peak identification.
* *S/N*: The Signal-to-Noise ratio of the actual signal.
* *BaselineModified*: The modified baseline after filtering/baseline estimation.
* *Quantification*: The peak's quantification value.
* *Full Width at Half Height*: The full width at half height characterizes the peak elongation.
* *IntegrationBegin*: The start of area integration.
* *IntegrationEnd*: The end of area integration.
* *Hit 1 Name*: The name of the first database match.
* *Hit 1 Similarity*: The similarity of the first database match.
* *Hit 1 Reverse*: The reverse similarity of the first database match.
* *Hit 1 Probability*: The probability of the first database match.
* *Hit 1 CAS*: The CAS id of the first database match.
* *Hit 1 Library*: The MSP library name of the database.
* *Hit 1 Id*: The native id of the first match.
* *Hit 1 Formula*: The sum formula of the first match.
* *Hit 1 Weight*: The molecular weight of the first match.
* *Hit 1 Contributor*: The contributor's name of the first match.
* *Spectra*: The spectrum of the peak.
* *Unmapped*: An unmapped column, unsupported by Maltcms (watch the log for these).
        
<h3>2D Chromatography Data</h3>
<h4>Supported column names for fused RT field</h4>
* *R.T. (s)*: Fused 2D retention times in RT field: e.g. *680 , 2.520*

<h4>Support for separate RT fields</h4>
* *1st Dimension Time (s)*: First separation column peak elution time, e.g. *680* 
* *2nd Dimension Time (s)*: Second separation column peak elution time, e.g. *4.125*

---

[1]: [Leco ChromaTOF](http://www.leco.com/products/separation-science/software-accessories/chromatof-software)
ChromaTOF is a registered trademark of LECO Corp., St. Joseph, Michigan, USA.
