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

# Declare an array of delimited input parameters.
ARRAY=("${@}")

# Declare a numeric constant of array elements.
ELEMENTS=${#ARRAY[@]}

JVMARGS=()
MALTCMSARGS=()
EXECARGS=("net.sf.maltcms.apps.Maltcms")

#define some common variables
USRCLSPATH=()
MXSIZE="-Xmx2G"
MSSIZE="-Xms128M"

#print help for arguments
function printHelp {
    printf "%-70s\n" "Usage: $0 [OPTION]... [MALTCMSARGS]..."
    printf "%4s %-20s %-44s\n" "-d64" "" "use 64bit jvm (default on 64 bit OS)"
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

# Process the parameter list.
for (( i = 0; i < ${ELEMENTS}; i++ )); do
  echo "  ARRAY["${i}"]=["${ARRAY[${i}]}"]"
  IPLUS=$((i+1)) #positional next parameter
  VAR="${ARRAY[${i}]}"
  case "$VAR" in
        -v)
            _V=1
            log "Verbose mode on!"
            ;;
        -exec|--execute)
            FILE="${ARRAY[${IPLUS}]}"
            if [ "${FILE##*.}" = "groovy" ]; then
                #add the Groovy Shell
                log "Using GroovyShell for execution of $VAR"
                EXECARGS=("groovy.lang.GroovyShell")
                EXECARGS+=("$FILE")
            else
                #otherwise use plain class name
                EXECARGS=("$FILE")
            fi
            #remove the next argument, since we processed it
            unset ARRAY[${IPLUS}]
            ;;
        --help)
            printHelp $0
            ;;
        -Xmx*)
            MXSIZE="$VAR"
            ;;
        -Xms*)
            MSSIZE="$VAR"
            ;;
        -D* | -XX* | -agent* | -X* | -d*)
            log "Java environment argument: $VAR"
            JVMARGS+=("$VAR")
            ;;
        *)
            MALTCMSARGS+=("$VAR")
            ;;
    esac
done

JVMARGS+=("$MSSIZE" "$MXSIZE")

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
    JAVA_LOCATION="$JAVA_HOME/bin/java"
    log "Found java from JAVA_HOME at $JAVA_LOCATION"
else
    JAVA_TMP=$(which java)
    if [[ "$?" -eq "0" ]]; then
        JAVA_LOCATION="$(which java)"
        log "Found java from environment at $JAVA_LOCATION"
    else
        loge "No java could be found! Please check your JAVA installation and that JAVA_HOME points to its location!"
    fi
fi


#environment arguments to java and maltcms, e.g. -DsomeOption=someValue
JVMARGS+=("-Dlog4j.configuration=file://$MALTCMSUSRDIR/cfg/log4j.properties")
JVMARGS+=("-Djava.util.logging.config.file=$MALTCMSUSRDIR/cfg/logging.properties")
JVMARGS+=("-Dmaltcms.home=$MALTCMSUSRDIR")

# set up classpath
#for i in $(ls $MALTCMSUSRDIR/lib/*.jar);
#do
#    if [ -z "$USRCLSPATH" ]; then
#            USRCLSPATH="$i"
#    else
#            USRCLSPATH="$USRCLSPATH:$i";
#    fi
#done
USRCLASSPATH="$MALTCMSUSRDIR/maltcms.jar"
if [ -n "$CLASSPATH" ]; then
    USRCLASSPATH="$CLASSPATH:$USRCLSPATH"
fi
#add the classpath
JVMARGS+=("-cp" "${USRCLASSPATH}")
#set up arguments
CMDLINE=("$JAVA_LOCATION")
CMDLINE+=("${JVMARGS[@]}")
CMDLINE+=("${EXECARGS[@]}")
CMDLINE+=("${MALTCMSARGS[@]}")
echo "Executing ${CMDLINE[@]}"
exec -a maltcms "${CMDLINE[@]}"
exit $?
