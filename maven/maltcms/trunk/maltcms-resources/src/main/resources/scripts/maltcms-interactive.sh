#!/bin/bash
echo "###############################################";
echo "# Welcome to Maltcms Interactive Command Line #";
echo "###############################################";
#path to the local maltcms installation
#either set to value of env var MALTCMSDIR
#or ask for user input
if [ -z "$MALTCMSDIR" ]; then
	echo "Please enter the path to Maltcms installation or add MALTCMSDIR to your bash profile:"
	read -e -p ">" MALTCMSUSRDIR;
	if [ -z "$MALTCMSUSRDIR" ]; then
		echo "No user defined directory for Maltcms installation entered, no default given, exiting!";
		exit 1;
	fi	
else
	MALTCMSUSRDIR=$MALTCMSDIR;
fi
#input directory
DEFAULTINDIR=$HOME;
echo "Please enter the directory path containing your input files (default $DEFAULTINDIR):";
read -e -p ">" INDIR;
if [ -z "$INDIR" ]; then
	INDIR=$DEFAULTINDIR; 
fi
echo "Input directory set to: $INDIR";
INDIR="-i $INDIR";

#recurse into input directory?
DEFAULTRECURSE="";
echo "Please enter 'yes' if you want to automatically include all files in subdirectories";
echo "of the input directory or hit enter to continue."
read -p ">" DORECURSE;
if [ -z "$DORECURSE" ]; then
		DORECURSE=$DEFAULTRECURSE;
else
	if [ "$DORECURSE" == "yes" ]; then
		DORECURSE="-r";
	fi
fi

#input filenames in input directory, absolute paths also work
DEFAULTFILENAMES="*.cdf";
echo "Please enter the filenames (without leading path),separated by commas (,).";
echo "You can also use a wildcard like *.cdf or *.mzxml to match all files.";
read -p ">" FILENAMES;
if [ -z "$FILENAMES" ]; then
	FILENAMES=$DEFAULTFILENAMES; 
fi
echo "Input files set to: $FILENAMES";
FILENAMES="-f $FILENAMES";

#output directory
DEFAULTOUTDIR=$HOME;
echo "Please enter the directory path for output of results (default $DEFAULTOUTDIR).";
read -e -p ">" OUTDIR;
if [ -z "$OUTDIR" ]; then
	OUTDIR=$DEFAULTOUTDIR; 
fi
echo "Output directory set to: $OUTDIR";
OUTDIR="-o $OUTDIR";

#anchor filenames, one for each input file
DEFAULTANCHORS="";
echo "Please enter the filenames containing your anchors (leave blank for no anchors).";
echo "You can also use a wildcard expression like *.txt to match all files in the input directory.";
read -e -p ">" ANCHORFILES;
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
read -e -p ">" CONFIGFILE;
if [ -n "$CONFIGFILE" ]; then
	echo "Configuration file to use: $CONFIGFILE";
	CONFIGFILE="-c $CONFIGFILE";
fi

#configure heapsize
DEFAULTHEAPSIZE="512m";
echo "Please enter the maximum amount of memory to use (default: $DEFAULTHEAPSIZE)";
echo "Values like 1G may also work"
read -p ">" HEAPSIZE;
if [ -z "$HEAPSIZE" ]; then
	echo "Heapsize to use: $HEAPSIZE";
	HEAPSIZE="$DEFAULTHEAPSIZE";
fi

#configure 32 or 64 bit vm 
DEFAULTBITS="32";
echo "Please enter 64 if you want to run a 64 bit virtual machine (default: $DEFAULTBITS)";
read -p ">" BITS;
if [ -z "$BITS" ]; then
	echo "Using $DEFAULTBITS bit";
	BITS="$DEFAULTBITS";
fi
CMDLINE="scripts/maltcms.sh -cp -mx $HEAPSIZE -$BITS -- \"$INDIR\" $DORECURSE \"$FILENAMES\" \"$OUTDIR\" \"$ANCHORFILES\" \"$CONFIGFILE\"";
echo "Using the following commandline: ";
echo "$CMDLINE";
export MALTCMSDIR=$MALTCMSUSRDIR;
cd "$MALTCMSDIR";
exec "$CMDLINE";
exit $!;
