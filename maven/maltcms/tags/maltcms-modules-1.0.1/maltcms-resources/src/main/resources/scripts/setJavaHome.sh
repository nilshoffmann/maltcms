#!/bin/bash
ARCH=`uname -a`
OSTYPE=`uname -s`
#echo -e "Running on $ARCH\n"
JAVA_HOME=""
case "$OSTYPE" in
	SunOS)
		JAVA_HOME="/vol/java-1.6/"
		;;
	Darwin)
		JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home/"	
		;;
	Linux)
		JAVA_HOME="/usr/lib/jvm/java-6-sun/"
		;;
	*)
		echo "Unknown OS $OSTYPE, please set the path to your Java-Installation by hand."
		echo "Save it in a file called javahome within maltcms basedir"
		;;
esac	
echo -e "$JAVA_HOME" > javahome
echo $JAVA_HOME
