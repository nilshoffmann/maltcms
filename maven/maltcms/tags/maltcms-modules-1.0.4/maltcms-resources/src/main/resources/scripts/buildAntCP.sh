#!/bin/bash
if [ "$1" != "" ]; then
	FPATH=$1
else
	FPATH=$(pwd)/scripts
fi
if [ -e antclspath ]; then
	rm antclspath
fi
for i in `ls $(echo -e $FPATH | sed -e s/scripts/lib/g)/*.jar`
do
	echo -en "<pathelement location=\"$i\"/>\n" >> antclspath;
done
