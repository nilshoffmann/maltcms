## Documentation

### GC-MS and GCxGC-MS Chromatogram Peaks Alignment Example

* [BiPACE and BiPACE2D Peak Alignment](bipace2d.html)

### Data Processing

Maltcms uses a pluggable IO-Provider infrastructure to map existing file formats to its internal 
data model. This is done by providing information about the logical organization of the file format 
into variables. The data model is organized similarly to the ANDI-MS data model, but more general and 
extensible. 

Files in the netCDF / ANDI-MS / ANDI-CHROM formats can be accessed from either local filesystems or from
remote http servers ([How to configure](http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/reference/HTTPservice.html)) without or with basic authentication.

Supported formats:

* (net)CDF (input & output), binary
* mzML (input & output), XML
* mzXML (input), XML
* mzData (input), XML
* csv (input), txt
* xls / xlsx, binary/XML
* OpenMS FeatureXML, XML

Maltcms processing pipelines are linear and non-branching. Although, you can couple any number of individual pipelines to realize branching structures. At the beginning of each pipeline are the input file fragments, containing or 
linking to the data that should be processed. Intermediate results can be saved individually with different semantics. One can create variables in a file fragment that have the same name as in a previous step of the pipeline or a previous pipeline. The older variable is then shadowed by the new one, allowing to substitute e.g. the original intensity profile with a filtered one. However, you are free to create variables with arbitrary names (following the netcdf cdm conventions). Every processing element in a maltcms pipeline, termed a 'fragment command' should declare the variables it requires for operation and it should also declare, which variables it provides. Optionally, a fragment command can declare additional variables that it needs to perform additional things, but which are not required for the basic operation.

* [Fragment Commands](fragmentCommands.html)
* [Workflow Configuration](workflowConfiguration.html)
* [Custom Workflow Elements](./development/customWorkflowElements.html)

In order to use more than one CPU on your local system, you can pass the option `-Dcross.Factory.maxthreads=N` to Maltcms on [the command line](../gettingStarted.html), where `N` should be the maximum number of CPUs you want Maltcms to use. If you do not want to set the number of threads for each invocation, you can configure the option in the file `cfg/factory.properties`.

It is also possible for some commands to be executed remotely, using the OpenGrid Scheduling system, or other DRMAA compatible scheduling systems. Currently, the DenseArrayProducer and the TICPeakFinder support this mode of operation, but other commands will soon follow.

* [Parallel Processing](./development/parallelProcessing.html)

### Developing with Maltcms
Maltcms uses Maven 3 as its build system. Maven offers the advantage to modularize a complex and large software system into seperate, independent units of functionality. 

* [Maven](./development/maven.html)
* [Development](./development/index.html)
