#!/bin/bash
export JARCH=""
export JBIN="\/vol\/java-1.6.0\/bin"
export EXEC="net.sf.maltcms.apps.Maltcms"
export MXSIZE="2G"
export MSSIZE="256M"
export MALTCMSARGS=""
export PROFILE=""
export USRCLSPATH=""

echo -e "Working in $(pwd)";
#Check, whether we are called from a maltcms installation
if [ -f "$(pwd)/maltcms.jar" ]; then
	echo -e "Found maltcms in current working directory $(pwd)";
	MALTCMSUSRDIR="$(pwd)";
else
#no, so check environment variable
	echo -e "maltcms.jar not found in working directory."
	echo -e "Checking for maltcms location MALTCMSDIR.";
	if [ -z "$MALTCMSDIR" ]; then
		echo "Please enter path to Maltcms installation or add MALTCMSDIR to your bash profile:"
		read MALTCMSUSRDIR;
		if [ -z "$MALTCMSUSRDIR" ]; then
			echo "No user defined directory for Maltcms installation entered, no default given, exiting!";
		exit 1;
	fi
		else
			MALTCMSUSRDIR="$MALTCMSDIR";
	fi
fi

LOG4J_LOCATION="-Dlog4j.configuration=file://$MALTCMSUSRDIR/cfg/log4j.properties"
if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "Using JAVA_HOME from environment"
    export JAVA_LOCATION="$JAVA_HOME"
else
	#Check if javahome exists => contains path to java
	if [ -f "$MALTCMSUSRDIR/javahome" ]; then
		echo "File javahome exists";
	else
		"$MALTCMSUSRDIR/scripts/setJavaHome.sh";
	fi	
	export JAVA_LOCATION="$(cat "$MALTCMSUSRDIR/javahome")";
fi
if [[ -n "$JAVA_LOCATION" ]]; then
	echo -e "Using java from ${JAVA_LOCATION}\n"
else
	echo "No java could be found!"
	exit 1
fi

function printHelp {
			echo -e "Usage: $0 [-64] [-mx ARG] [-ms ARG] [-exec ARG|--execute ARG] [-?|--help] [MALTCMSARGS]"
			echo -e "\t-64 -> uses 64bit jvm"
			echo -e "\t-mx ARG -> uses -XmxARG as maximum heapsize"
			echo -e "\t-ms ARG -> uses -XmsARG as minimum heapsize"
			echo -e "\t-exec ARG|--execute ARG -> execute the given base class,"
		       	echo -e "\t\t\tif it contains a main method"
			echo -e "\t\t\te.g. '-exec mypackage/MyClass'"
			echo -e "\t-?|--help -> display this help"
			echo -e "\tMALTCMSARGS -> hand all remaining arguments over to Maltcms"
			exit -1
}

if [ $# -eq 0 ]; then
	printHelp $1
fi

while [ $# -gt 0 ]; do
	case "$1" in
		-64)
			export JARCH="-d64"
			;;
		-exec|--execute)
			shift
			export EXEC="$1"
			;;
		-profile)
			export PROFILE="-agentlib:hprof=heap=sites,file=hprof.out,format=b,cpu=samples,interval=20"
			;;		
		-mx)	
			shift
			export MXSIZE="$1"
			;;
		-ms)
			shift
			export MSSIZE="$1"
			;;
                -"?"|--help)
			printHelp $0
			;;
		*)
			echo "Running a $JARCH VM with -Xmx $MXSIZE"
			#Check for clspath file
			if [ -f "$MALTCMSUSRDIR/clspath" ]; then
				echo "File clspath exists";
			else
				"$MALTCMSUSRDIR"/scripts/buildCP.sh
			fi
			USRCLSPATH="$CLASSPATH:$(cat "$MALTCMSUSRDIR/clspath")"
			echo -e "Passing args to $EXEC"
			echo -e "$@"
			sleep 1
			$JAVA_LOCATION/bin/java -cp "$USRCLSPATH" $PROFILE -Xms$MSSIZE -Xmx$MXSIZE $JARCH "$LOG4J_LOCATION" "$EXEC" "$@" 
			exit $?
			;;	
	esac
	shift
done

exit 1


