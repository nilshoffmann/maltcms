#!/usr/bin/env bash
echo -e "Current path: $PATH\n"
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
if [ -z "$1" ]; then
	echo -e "No argument given, exiting!"
	exit 1
fi
if type -p java; then
    echo found java executable in PATH
    JAVA=$(which java)
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME     
    JAVA="$JAVA_HOME/bin/java"
else
    echo could not locate java
    exit 1
fi

COMMAND=$1
FILEPATH=$(dirname $COMMAND)
SCRIPT=$(basename $COMMAND)
shift
echo -e "COMMAND: $COMMAND"
echo -e "PATH: $PATH"
#cd $PATH
cd $FILEPATH
$JAVA -cp "$MALTCMSUSRDIR/maltcms.jar" groovy.lang.GroovyShell "$SCRIPT" "$@"
