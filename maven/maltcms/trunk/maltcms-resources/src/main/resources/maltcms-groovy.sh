#!/usr/bin/env bash
SCRIPTFILE=$(readlink -f $0)
SCRIPTDIR=$(dirname $SCRIPTFILE)
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
$MALTCMSUSRDIR/maltcms.sh -exec groovy.lang.GroovyShell $@
exit $?
