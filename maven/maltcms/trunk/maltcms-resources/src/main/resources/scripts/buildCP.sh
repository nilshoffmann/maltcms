#!/bin/bash
if [ "$1" != "" ]; then
        FPATH="$1"
else
        FPATH="$(pwd)/scripts"
fi
if [ -e clspath ]; then
        rm clspath
fi
for i in `ls lib/*.jar`
do
        echo -n ":$i" >> clspath;
done

