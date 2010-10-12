#!/bin/bash
echo "###############################################";
echo "# Welcome to Maltcms Interactive Command Line #";
echo "###############################################";
#path to the local maltcms installation
#either set to value of env var MALTCMSDIR
#or ask for user input
if [ -z "$MALTCMSDIR" ]; then
	echo "Please enter path to Maltcms installation or add MALTCMSDIR to your bash profile:"
	read MALTCMSUSRDIR;
	if [ -z "$MALTCMSUSRDIR" ]; then
		echo "No user defined directory for Maltcms installation entered, no default given, exiting!";
		exit 1;
	fi	
else
	MALTCMSUSRDIR=$MALTCMSDIR;
fi
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
echo "Please enter the filenames containing your anchors (leave blank for no anchors):";
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

#configure heapsize
DEFAULTHEAPSIZE="512m";
echo "Please enter the maximum amount of memory to use (default: $DEFAULTHEAPSIZE)";
echo "Values like 1G may also work"
read HEAPSIZE;
if [ -z "$HEAPSIZE" ]; then
	echo "Heapsize to use: $HEAPSIZE";
	HEAPSIZE="$DEFAULTHEAPSIZE";
fi

#configure 32 or 64 bit vm 
DEFAULTBITS="32";
echo "Please enter 64 if you want to run a 64 bit virtual machine (default: $DEFAULTBITS)";
read BITS;
if [ -z "$BITS" ]; then
	echo "Using $DEFAULTBITS bit";
	BITS="$DEFAULTBITS";
fi
CMDLINE="scripts/maltcms.sh -cp -mx $HEAPSIZE -$BITS -- $INDIR $FILENAMES $OUTDIR $ANCHORFILES $CONFIGFILE";
echo "Using the following commandline: ";
echo "$CMDLINE";
cd $MALTCMSUSRDIR;
exec $CMDLINE;
exit $!;
