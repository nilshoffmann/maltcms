#!/usr/bin/env bash
# Maltcms startup script for Unix/MacOS X/Linux, adapted from www.gradle.org gradle.sh
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

_V=0

function log () {
    if [[ $_V -eq 1 ]]; then
        echo "$@"
    fi
}

function loge () {
    echo "$@"
    exit 1
}

#store the directory from which we were invoked
WORKINGDIR="`pwd`"
#change to the resolved scriptfile location
cd "`dirname \"$SCRIPTFILE\"`/.."
SCRIPTDIR="`pwd -P`"
cd "$WORKINGDIR"

#define some common variables
JBIN="\/vol\/java-1.6.0\/bin"
EXEC="net.sf.maltcms.apps.Maltcms"
MALTCMSARGS=""
USRCLSPATH=""
MXSIZE="-Xmx2G"
MSSIZE="-Xms128M"

#print help for arguments
function printHelp {
    printf "%-70s\n" "Usage: $0 [OPTION]... [MALTCMSARGS]..."
    printf "%4s %-20s %-44s\n" "-64" "" "use 64bit jvm (default on 64 bit OS)"
    printf "%4s %-20s %-44s\n" "" "-XmxARG" "maximum heapsize (M for MBytes, G for GBytes)"
    printf "%4s %-20s %-44s\n" "" "-XmsARG" "minimum heapsize"
    printf "%4s %-20s %-44s\n" "" "-exec ARG" "execute the given base class,"
    printf "%4s %-20s %-44s\n" "" "--execute ARG" "if it contains a main method"
    printf "%4s %-20s %-44s\n" "" "" "e.g. '-exec mypackage.MyClass'"
    printf "%4s %-20s %-44s\n" "" "-DKEY=VALUE" "set system property KEY to value VALUE,"
    printf "%4s %-20s %-44s\n" "" "" "may appear multiple times"
    printf "%4s %-20s %-44s\n" "" "--help" "display this help and exit"
    printf "%4s %-20s %-44s\n" "-?" "" "display maltcms help and exit"
    printf "%4s %-20s %-44s\n" "-h" "" ""
    printf "%4s %-20s %-44s\n" "-v" "" "enable verbose mode"
    printf "%4s %-20s %-44s\n" "" "MALTCMSARGS" "hand all remaining arguments over to Maltcms"
    exit 1
}

if [ $# -eq 0 ]; then
	printHelp $1
fi

while [ $# -gt 0 ]; do
    case "$1" in
        -v) 
            _V=1
            log "Verbose mode on!"
            shift
            ;;
        -exec|--execute)
            shift
            FILE="$1"
            if [ "${FILE#*.}" = "groovy" ]; then
                #add the Groovy Shell
                log "Using GroovyShell for execution of $1"
                EXEC="groovy.lang.GroovyShell $1"
            else
                #otherwise use plain class name
                EXEC="$1"
            fi
            shift
            ;;
        --help)
            printHelp $0
            ;;
        -Xmx*)
            MXSIZE="$1"
            shift
            ;;
        -Xms*)
            MSSIZE="$1"
            shift
            ;;
        -D* | -XX* | -agent* | -X* | -d*)
            log "Java environment argument: $1"
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

#Check, whether we are called from a maltcms installation
if [ -f "$SCRIPTDIR/maltcms.jar" ]; then
    log "Found maltcms in script home directory $SCRIPTDIR";
    MALTCMSUSRDIR="$SCRIPTDIR";
else
#no, so check environment variable
    if [ -n "$MALTCMS_HOME" ]; then
        if [ -f "$MALTCMS_HOME/maltcms.jar" ]; then
            log "Found maltcms location below $MALTCMS_HOME!"
            MALTCMSUSRDIR="$MALTCMS_HOME";
        else
            loge "Maltcms does not seem to exist under $MALTCMS_HOME!";
        fi
    elif [ -n "$MALTCMSDIR" ]; then
        #MALTCMSDIR is deprecated, please use MALTCMS_HOME to indicate installation directory of maltcms
        echo -e "WARNING: Checking for maltcms location MALTCMSDIR from environment."
        echo -e "WARNING: MALTCMSDIR is DEPRECATED, please set maltcms installation location in environment variable MALTCMS_HOME!";
        if [ -f "$MALTCMSUSRDIR/maltcms.jar" ]; then
            log "Found maltcms location below $MALTCMSUSRDIR!"
        else
            loge "Maltcms does not seem to exist under $MALTCMSUSRDIR!";
        fi

        echo "Please enter path to Maltcms installation or add MALTCMSDIR to your bash profile:"
        read MALTCMSUSRDIR;
        if [ -z "$MALTCMSUSRDIR" ]; then
            loge "No user defined directory for Maltcms installation entered, no default given, exiting!";
        fi
    else
        if [ -f "$MALTCMSDIR/maltcms.jar" ]; then
            MALTCMSUSRDIR="$MALTCMSDIR";
        else
            loge "Maltcms does not seem to exist under $MALTCMSDIR! Please check MALTCMSDIR for correctness!";
        fi
    fi
fi

if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    log "Found java from JAVA_HOME!"
    JAVA_LOCATION="$JAVA_HOME/bin/java"
else
    JAVA_TMP=$(which java)
    if [[ "$?" -eq "0" ]]; then
        log "Found java from environment!"
        JAVA_LOCATION="$(which java)"
    else
        loge "No java could be found! Please check your JAVA installation and that JAVA_HOME points to its location!"
    fi
fi


#environment arguments to java, e.g. -DsomeOption=someValue
ENVARGS="-Dlog4j.configuration=file://$MALTCMSUSRDIR/cfg/log4j.properties -Djava.util.logging.config.file=$MALTCMSUSRDIR/cfg/logging.properties"

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
ARGS="$MXSIZE $MSSIZE -Dmaltcms.home=$MALTCMSUSRDIR"
if [ -n "$ENVARGS" ]; then
    #echo "Using system properties: $ENVARGS"
    ARGS="$ARGS $ENVARGS"
fi
ARGS="$ARGS -cp $USRCLSPATH $EXEC $MALTCMSARGS"
log "Executing $JAVA_LOCATION $ARGS"
$JAVA_LOCATION $ARGS
exit $?
