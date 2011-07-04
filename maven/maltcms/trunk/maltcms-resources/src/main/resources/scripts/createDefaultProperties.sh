#!/bin/bash
BASEDIR="."
if [ -z "$1" ];
then
	BASEDIR="."
else
	BASEDIR="$1"	
fi
for CLASS in $(java -jar maltcms.jar -l cross.commands.fragments.AFragmentCommand | tail -n +2 | head -n -1); 
do 
	java -cp maltcms.jar cross.io.PropertyFileGenerator $BASEDIR $CLASS
done
