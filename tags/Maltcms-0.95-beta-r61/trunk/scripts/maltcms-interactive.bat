@echo off
echo ###############################################
echo # Welcome to Maltcms Interactive Command Line #
echo ###############################################
#path to the local maltcms installation
MALTCMSINST="/vol/maltcms/release/"
#input directory
DEFAULTINDIR=$HOME;
echo "Directory containing input files (default $DEFAULTINDIR):";
read INDIR;
if [ -z "$INDIR" ]; then
	INDIR=$DEFAULTINDIR; 
fi
echo "Input directory set to: $INDIR";
INDIR="-i $INDIR";

#input filenames in input directory, absolute paths also work
DEFAULTFILENAMES="*.cdf";
echo "Please enter the filenames (without leading path),separated by commas (,).";
echo "You can also use a wildcard like *.cdf or *.mzxml to match all files.";
read FILENAMES;
if [ -z "$FILENAMES" ]; then
	FILENAMES=$DEFAULTFILENAMES; 
fi
echo "Input files set to: $FILENAMES";
FILENAMES="-f $FILENAMES";

#output directory
DEFAULTOUTDIR=$HOME;
echo "Directory for output (default $DEFAULTOUTDIR):";
read OUTDIR;
if [ -z "$OUTDIR" ]; then
	OUTDIR=$DEFAULTOUTDIR; 
fi
echo "Output directory set to: $OUTDIR";
OUTDIR="-o $OUTDIR";

#anchor filenames, one for each input file
DEFAULTANCHORS="";
echo "Please enter the filenames containing your anchors:";
read ANCHORFILES;
if [ -z "$ANCHORFILES" ]; then
	ANCHORFILES=$DEFAULTANCHORS; 
fi
echo "Anchor files set to: $ANCHORFILES";
if [ -n "$ANCHORFILES" ]; then
	ANCHORFILES="-a $ANCHORFILES";		 
fi

#use a custom configuration
DEFAULTCONFIG="";
echo "Please enter the filename of custom properties (default: $DEFAULTCONFIG)";
read CONFIGFILE;
if [ -n "$CONFIGFILE" ]; then
	echo "Configuration file to use: $CONFIGFILE";
	CONFIGFILE="-c $CONFIGFILE";
fi

CMDLINE="scripts/run.sh -cp -mx 10G -64 -- $INDIR $FILENAMES $OUTDIR $ANCHORFILES $CONFIGFILE";
echo "Using the following commandline: ";
echo "$CMDLINE";
cd $MALTCMSINST;
exec $CMDLINE;
exit $!;
