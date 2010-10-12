#!/bin/bash
if [ "$1" != "" ]; then
	FPATH=$1
else
	FPATH=$(pwd)/scripts
fi
if [ -e clspath ]; then
	rm clspath
fi
for i in `ls $(echo -e $FPATH | sed -e s/scripts/lib/g)/*.jar`
do 
	echo -n ":$i" >> clspath;
done
#for i in `ls $(echo -e $FPATH | sed -e s/scripts/cfg/g)/*.properties`
#do
#	echo -n ":$i" >> clspath;
#done
#echo -e $FPATH
PATH=`echo -e $FPATH | sed -e s/scripts/bin/g`
echo -e ":$PATH" >> clspath
#export CLASSPATH="$CLASSPATH$(cat clspath)"
#echo $CLASSPATH
