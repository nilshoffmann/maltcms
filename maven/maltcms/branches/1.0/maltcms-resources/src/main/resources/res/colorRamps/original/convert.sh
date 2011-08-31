#!/bin/bash
for i in $1; do
tr -s " " < $i | sed -e "s/^ //g" > $i.tmp
cut -d " " -f-4 $i.tmp > $i.new
cut -d " " -f5-8 $i.tmp >> $i.new
cut -d " " -f9-12 $i.tmp >> $i.new
cut -d " " -f13-16 $i.tmp >> $i.new
done
