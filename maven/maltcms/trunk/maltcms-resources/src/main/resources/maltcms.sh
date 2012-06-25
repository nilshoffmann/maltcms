#!/usr/bin/env bash
SCRIPTFILE="$0"
# Follow relative symlinks to resolve script location
while [ -h "$SCRIPTFILE" ] ; do
    ls=`ls -ld "$SCRIPTFILE"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        SCRIPTFILE="$link"
    else
        SCRIPTFILE=`dirname "$SCRIPTFILE"`"/$link"
    fi
done
#store the directory from which we were invoked
WORKINGDIR="`pwd`"
#change to the resolved scriptfile location
cd "`dirname \"$SCRIPTFILE\"`"
SCRIPTDIR="`pwd -P`"
cd "$WORKINGDIR"

export JARCH=""
export JBIN="\/vol\/java-1.6.0\/bin"
export EXEC="net.sf.maltcms.apps.Maltcms"
export MALTCMSARGS=""
export MSSIZE="128M"
export MXSIZE="2G"
export PROFILE=""
export USRCLSPATH=""

#Check, whether we are called from a maltcms installation
if [ -f "$SCRIPTDIR/maltcms.jar" ]; then
	echo -e "Found maltcms in script home directory $SCRIPTDIR";
	MALTCMSUSRDIR="$SCRIPTDIR";
else
#no, so check environment variable
	echo -e "Checking for maltcms location MALTCMSDIR from environment.";
	if [ -z "$MALTCMSDIR" ]; then
		echo "Please enter path to Maltcms installation or add MALTCMSDIR to your bash profile:"
		read MALTCMSUSRDIR;
		if [ -z "$MALTCMSUSRDIR" ]; then
			echo "No user defined directory for Maltcms installation entered, no default given, exiting!";
				exit 1;
		else
			if [ -f "$MALTCMSUSRDIR/maltcms.jar" ]; then
				echo "Found maltcms location below $MALTCMSUSRDIR!"
			else
				echo "Maltcms does not seem to exist under $MALTCMSUSRDIR!";
				exit 1
			fi
		fi
	else
		if [ -f "$MALTCMSDIR/maltcms.jar" ]; then
			MALTCMSUSRDIR="$MALTCMSDIR";
		else
			echo "Maltcms does not seem to exist under $MALTCMSDIR! Please check MALTCMSDIR for correctness!";
			exit 1
		fi
	fi
fi

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
   	echo "Found java from JAVA_HOME!"
	export JAVA_LOCATION="$JAVA_HOME"
else
	JAVA_TMP=$(which java)
	if [[ "$?" -eq "0" ]]; then
		echo "Found java from environment!"
		export JAVA_LOCATION="$(readlink -f $(which java))"
	else
		echo "No java could be found! Please check your JAVA installation and that JAVA_HOME points to its location!"
		exit 1
	fi
fi

function printHelp {
			echo -e "Usage: $0 [-64] [-mx ARG] [-ms ARG] [-exec ARG|--execute ARG] [-DKEY=VALUE] [--help] [-?|-h] [MALTCMSARGS]"
			echo -e "\t-64 -> use 64bit jvm"
			echo -e "\t-mx ARG -> use -XmxARG as maximum heapsize"
			echo -e "\t-ms ARG -> use -XmsARG as minimum heapsize"
			echo -e "\t-exec ARG|--execute ARG -> execute the given base class,"
		       	echo -e "\t\t\tif it contains a main method"
			echo -e "\t\t\te.g. '-exec mypackage.MyClass'"
			echo -e "\t-DKEY=VALUE -> set system property KEY to value VALUE,"
			echo -e "\t\t\tmay appear multiple times"
			echo -e "\t--help -> display this help"
			echo -e "\t-?|-h -> display maltcms help"
			echo -e "\tMALTCMSARGS -> hand all remaining arguments over to Maltcms"
			exit -1
}

if [ $# -eq 0 ]; then
	printHelp $1
fi

#environment arguments to java, e.g. -DsomeOption=someValue
ENVARGS="-Dlog4j.configuration=file://$MALTCMSUSRDIR/cfg/log4j.properties -Djava.util.logging.config.file=$MALTCMSUSRDIR/cfg/logging.properties"

while [ $# -gt 0 ]; do
	case "$1" in
		-64)
			export JARCH="-d64"
			shift
			;;
		-exec|--execute)
			shift
			export EXEC="$1"
			shift
			;;
		-mx)	
			shift
			export MXSIZE="$1"
			shift
			;;
		-ms)
			shift
			export MSSIZE="$1"
			shift
			;;
        	--help)
			printHelp $0
			;;
		-D* | -XX* | -agent*)
			echo "Java environment argument: $1"
			if [ -z "$ENVARGS" ]; then
		            ENVARGS="$1"
		        else
	        	    ENVARGS="$ENVARGS $1"
			fi
			shift
			;;
		*)
			if [ -z "$MALTCMSARGS" ]; then
	  		    MALTCMSARGS="$1"
		        else
		            MALTCMSARGS="$MALTCMSARGS $1"
			fi
			shift
			;;	
	esac
done

echo -e "Running a $JARCH VM with -Xms$MSSIZE -Xmx$MXSIZE"
# set up classpath
for i in $(ls $MALTCMSUSRDIR/lib/*.jar);
do
	if [ -z "$USRCLSPATH" ]; then
		USRCLSPATH="$i"
	else
		USRCLSPATH="$USRCLSPATH:$i";
	fi
done
if [ -n "$CLASSPATH" ]; then
	USRCLASSPATH="$CLASSPATH:$USRCLSPATH"
fi
#set up arguments
ARGS="-Xmx$MXSIZE -Xms$MSSIZE"
if [ -n "$ENVARGS" ]; then
	echo "Using system properties: $ENVARGS"
	ARGS="$ENVARGS"
fi
if [ -n "$PROFILE" ]; then
	echo "Using profiler to collect runtime execution statistics"
	ARGS="$ARGS $PROFILE"
fi
ARGS="$ARGS -cp $USRCLSPATH $EXEC $MALTCMSARGS"
#echo -e "Executing $JAVA_LOCATION/bin/java $ENVARGS -cp lib/*.jar $EXEC $MALTCMSARGS"
$JAVA_LOCATION $ARGS
exit $?


