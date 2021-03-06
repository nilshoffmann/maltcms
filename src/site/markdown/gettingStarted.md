# Download

1. Please download and install Java Runtime Environment (JRE) or 
    JDK version 1.8.0 or newer for your specific platform and 
    operating system from 
    [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

2. Then [download Maltcms](https://sourceforge.net/projects/maltcms/files/maltcms/maltcms-${project.version}) and follow the installation instructions.

# Installation Instructions

Maltcms currently has no automatic installation script. In order to 
set everything up correctly, please follow the instructions for your
operating system.

## Unix/Linux/MacOSX

Extract the downloaded zip archive to a directory of your choice. In
order to have maltcms.sh available on your command prompt, set the
following in your $HOME/.profile or $HOME/.bashrc 
(or in any other appropriate shell configuration file).

    export PATH=$PATH:/PATH/TO/EXTRACTED/ARCHIVE/bin

Maltcms can be launched via the maltcms.sh script in its home bin
directory. This will use the java executable available via the 
environment variable JAVA_HOME. Otherwise, the script will try to find
the location of your java installation by checking, if the 'java' 
command is available on your path.

The scripts will prompt for the installation directory of 
Maltcms, if the location of maltcms.jar can not be determined by the
script automatically, it will check the environment variable 
MALTCMS_HOME. If that does not exist, the script will prompt you for 
the explicit location. 

If you would like to install the maltcms.sh script and other scripts 
apart from the maltcms installation location, we suggest to add 

    export MALTCMS_HOME=/path/to/your/maltcms/


to your $HOME/.bashrc or $HOME/.profile file.
Alternatively, simply overwrite the environment variable within
maltcms.sh to set it to a fixed location. For backwards compatibility,
MALTCMSDIR is still supported, but maltcms.sh will print a deprecation
warning!

## Windows

Extract the downloaded zip archive to a directory of your choice. In order
to have maltcms.bat available on your command prompt, please follow the
instructions appropriate for your installed operating system. You may need
system administrator privileges to change system variables!

### Windows 7 / 8 / 10

Select 'Computer' from the Start menu and choose 'System Properties' from
the top level actions bar or from the context menu. Click on 
'Advanced system settings' and select the 'Advanced' tab in the dialog.
Click on 'Environment Variables' and under 'System Variables' or 
'User Variables', locate the 'PATH' variable, and double-click to edit it.
If no variable named 'PATH' is available in the 'User Variables' section,
select new, name the variable 'PATH'.

### Windows Vista

Right click on your 'My Computer' icon on the desktop and select 
'Properties' from the popup-menu. Click on 'Advanced system settings' and
select the 'Advanced' tab in the dialog. Click on 'Environment Variables'
and under 'System Variables' or 'User Variables', locate the 'PATH' 
variable, and double-click to edit it. If now variable named 'PATH' is
available in the 'User Variables' section, select new, name the variable
'PATH'.

### Windows XP

Select 'Start' and click 'Control Panel'. Then go to 'System' and click on
'Advanced'. Click on 'Environment Variables' and under 'System Variables'
or 'User Variables', locate the 'PATH' variable, and double-click to edit
it. If now variable named 'PATH' is available in the 'User Variables'
section, select new, name the variable 'PATH'.

#### Editing the Path Variable

Add the absolute path of the Maltcms directory to the end of the 'Variable value' like so:

Old:

    C:\Program Files;C:\Winnt;C:\Winnt\System32

New:

    C:\Program Files;C:\Winnt;C:\Winnt\System32;C:\Path\To\Maltcms\bin

Finally, open a new command prompt by typing the 'Windows' or 'Meta' key
on your keyboard. Then type 'cmd.com' and hit enter. A new command prompt
will open. Type 'maltcms', hit 'Enter' and you should see maltcms' help
appear. It may be necessary to log out and back in before the changed 
settings take effect.

The script will figure out its installation path from the call
automatically. It will prompt for the installation directory of 
maltcms, if you call Maltcms from outside the installation directory and
the environment variable MALTCMS_HOME is not set. We recommend to add 
'MALTCMS_HOME' as an additional 'System Variable', by defining a new 
variable in the 'Environment Variables' dialog under 'User variables'
with the name 'MALTCMS_HOME' and the absolute path of the Maltcms 
installation directory as its value.

# Running Maltcms

The following section requires a command prompt (shell), indicated by
the prefixed '>', which should not be typed into the prompt. Additionally,
this section requires that you have installed Maltcms and that it is 
available from your path.

If you are using Windows, please replace 'maltcms.sh' with 'maltcms.bat'.

## Running ChromA/ChromA4D
    
Pipeline location: As of Maltcms 1.2.1, pipeline *.properties files
have been relocated to cfg/pipelines and have been renamed to .mpl 
(Maltcms PipeLine) to better distinguish them from standard 
configuration files. However, the old *.properties will still work 
without change. You may consider to rename them for consistency though.

## ChromA

To execute the standard pipeline for GC- and LC-MS, use 

    >maltcms.sh -c cfg/pipelines/chroma.mpl -i INPUTDIR -o OUTPUTDIR -f FILES

## ChromA4D

To execute the available pipelines for GCxGC-MS, use 

    >maltcms.sh -c cfg/pipelines/chroma4D.mpl -i INPUTDIR -o OUTPUTDIR -f FILES

If you do not supply any arguments, Maltcms will print all available 
arguments with a short explanation.

    >maltcms.sh -- -h

prints command-line options for the script with explanations.

## Advanced Usage
    
You can start the 32 bit commandline version by typing

    >maltcms.sh

within the bin dir below your Maltcms installation directory, which will
print out the input options that you can supply.

Alternatively, on a 64 bit system and with 64 bit VM you can call

    >maltcms.sh -d64 ...

Sometimes, the default amount of memory used by the JAVA VM is 
not sufficient. You can then call

    >maltcms.sh -Xms1G -Xmx2G ...

where -Xms1G sets the minimum amount of memory used by the VM to 
1GByte of Ram and -Xmx2G sets the limit of the maximum amount of memory 
available to maltcms to 2GBytes. If you have more RAM installed, you can
always increase the latter limit.

If you use java on a 32 bit architecture and a 32 bit VM, more than 2G 
might not be available. Using java on 64 bit architectures
and with a 64 bit VM allows larger amounts of memory to be
allocated.

In order to define custom properties, you need to pass the -c argument
to Maltcms:

    >maltcms.sh ... -c cfg/pipelines/myCustomPipeline.mpl -i INPUTDIR -o OUTPUTDIR -f FILES

will use the pipeline configuration 'myCustomPipeline.mpl' below the current
directory. Please note that from version 1.2.1 onwards, Maltcms pipeline 
configurations are stored below cfg/pipelines and are distinguishable by the
'.mpl' file extension (mpl -> Maltcms PipeLine). However, the new file 
extension is just a convention.

Recommended arguments for Maltcms are:

    -i INDIR (e.g. /vol/data)
    -o OUTDIR (e.g. /vol/output)
    -f FILES (e.g. "*.cdf")

If no INDIR is given, FILES are resolved against the current working
directory. If no OUTDIR is given, processing results will be created
below the current working directory in a directory following the 
convention 'maltcmsOutput/USERNAME/DATETIME/'.

In order to recurse into the INDIR, you can add '-r' to the command
line. As an alternative to defining the input directory explicitly and
using a file glob expression (only *.EXT expressions will currently 
work), you can also define a comma separated list of absolute or 
relative file paths. Relative paths are resolved from the current
working directory. Please note that paths containing spaces can only
be handled, if the whole argument (all paths separated by commas) is
flanked in '"'s. 

You can alternatively omit INDIR, if you fully qualify the path before each 
file provided with -f. Theses files can also have a wildcard expression 
as their name, e.g. "*.cdf". You can use '-f FILES' multiple times to
supply different base directories with different file wildcard expressions
to Maltcms. If you want to include all files below those base directories
matching the wildcard, you should also supply the '-r' option.

# Other Features

## Graphical Wizard UI

As of version 1.1 of Maltcms, the graphical wizard is no longer 
supported. Please have a look at our Rich Client Software Maui

## Extending Maltcms with custom code

Maltcms allows to add functionality by adding custom jar files
to the runtime classpath.

The classpath in maltcms.jar is created at 
build time and will not contain those jars, so you need to add them
manually to the runtime classpath with the '-cp' option supplied to
java, or by using the 'maltcms.sh' script, which will pick these up
automatically.

## Scripting with Groovy

To simply use Maltcms in a higher level workflow, you can easily write
scripts in the Groovy programming language (http://groovy.codehaus.org/).
These scripts can be executed by calling 

    maltcms.sh -exec path/to/script.groovy ARGS

from the Maltcms installation base directory, where ARGS are optional
parameters to your script. An example of such workflows can be found 
at [Github](https://github.com/nilshoffmann/maltcms-evaluation).

## Input data formats

Currently, netCDF compatible input and output is 
supported, mzXML input works reliably for MS1 data, output is
currently redirected to netCDF format. Additionally, mzData is 
supported as input format, but again, output is redirected to netCDF
format. Recently, read support for mzML has been added via the jmzml 
library.

To get some insight into the parameters used by Maltcms and possible
alternatives, consult the properties files in cfg/, especially

* factory.properties
* graphics.properties
* io.properties

and for logging options

* log4j.properties
* logging.properties

Properties and settings for individual commands in a processing
pipeline are located in the respective xml file below 

    cfg/pipelines/xml/

An overview of the available parameters for each pipeline element
is available at [SourceForge](http://maltcms.sourceforge.net/maven/maltcms/${project.version})
for releases and at [Maltcms.de](http://maltcms.de/staging/maltcms) for the latest development
version.

Custom configuration files can be provided to maltcms.sh/maltcms.bat,
as well as to maltcms.jar using the -c option. Those custom options
then override the default values. Additionally, java supports the 
setting of environment properties via the -DmyProp=myValue switch. 
These will always override properties with the same name in user-
supplied and default configurations.

# Example data

Example data for maltcms in netcdf format along with alignment
anchors can be downloaded individually from
[BiBiServ](http://bibiserv.techfak.uni-bielefeld.de/chroma/download.html).

Alternatively, 1D and 2D chromatogram data can be downloaded from
[SourceForge](http://sourceforge.net/projects/maltcms/files/maltcms/example-data/).

# File format for Anchors

Anchor files start with a file designation in their first line:
Path can be omitted, if ORIGINAL_DATA_FILE and anchor file are both
located in input base directory (cmdline option -i). Otherwise, the
absolute path to the original file has to be given.

    >NAME_OF_ORIGINAL_DATA_FILE.cdf

The second line defines the column names, separated by 'tab' (\t):

    Name	RI	RT	Scan

All following lines contain information for one anchor each:

    RI1	-	-	230
    RI2	-	-	430

etc.

Note that columns RI and RT are optional, Scan and Name are required.
Anchor names should be identical for matched compounds. Fields are again
separated by 'tab' (\t).

## Full example

File Example1.txt would look like this:

    >Example1.cdf
    Name	RI	RT	Scan
    RI1	-	-	230
    RI2	-	-	430
    RI3	-	-	600
	
File Example2.txt would look like this:

    >Example2.cdf
    Name	RI	RT	Scan
    RI1	-	-	234
    RI2	-	-	437
    RI3	-	-	598
