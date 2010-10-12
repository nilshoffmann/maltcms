#!/bin/bash
REVISION=`svn info | grep -e Revision | sed -e "s/Revision: //"`
DATE=`LANG=en_US.UTF-8 date +%Y_%b_%d`
sed -i -e "s/application.version.revision=-r[0-9]*/application.version.revision=-r$REVISION/" cfg/application.properties
sed -i -e "s/application.build.date=-[0-9A-Za-z_]*/application.build.date=-$DATE/" cfg/application.properties
cut -f 2 -d= cfg/application.properties > versionfile;
grep -v "\\$" versionfile > tmpversionfile;
paste -s -d\\0 tmpversionfile > versionfile;
rm tmpversionfile;
sed -i -e "s/PROJECT_NUMBER\W*=.*/PROJECT_NUMBER=$(cat versionfile)/g" Doxyfile
cat versionfile;
