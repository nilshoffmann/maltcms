# This is the Maltcms maven3 project

**This project is currently inactive.**

[![Build Status](https://travis-ci.org/nilshoffmann/maltcms.svg?branch=master)](https://travis-ci.org/nilshoffmann/maltcms)
[![Release Version](https://img.shields.io/github/release/nilshoffmann/maltcms.svg)](https://github.com/nilshoffmann/maltcms/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/nilshoffmann/maltcms/total.svg)](https://github.com/nilshoffmann/maltcms/releases/latest)
[![License](https://img.shields.io/badge/license-LGPL--3.0-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
[![License](https://img.shields.io/badge/license-EPL--1.0-blue.svg)](https://www.eclipse.org/legal/epl-v10.html)

[:blue_book: Documentation](http://nilshoffmann.github.io/maltcms/)

Within this directory, you will find all the submodules, which 
make up Maltcms. 
General configuration for all modules can be found within the 
file pom.xml in the same directory as this README. Module-specific
configuration can be found below each module's directory in the 
corresponding pom.xml file. 

## Prerequisites for Maltcms
- Java JDK 8 (NOT JRE), as of release version 1.4.0
  http://www.oracle.com/technetwork/java/javase/downloads/index.html
- Maven 3
  http://maven.apache.org/download.html
- Any IDE supporting maven 2/3 integration and subversion support
  NetBeans 8.+
  	http://netbeans.org/
  Eclipse 4.+
  	http://eclipse.org/
	(requires additional m2e plugin and 
	 subclipse/subversive plugins)
  IntelliJ Idea 14 CE
  	http://www.jetbrains.com/idea/

The remainder of this README contains task descriptions and the corresponding 
commands required to perform those tasks. Tasks marked with '*' can only be 
executed by registered developers. In order to register, you need to have a
sourceforge account and apply for membership within the project maltcms.

## User registration:
Create a new user name at
https://sourceforge.net/user/registration

Then, apply to join maltcms by sending a mail to the project admin with your
sourceforge username:
http://sourceforge.net/users/nilshoffmann/ 

You will then receive access to the project's subversion repository as well as
to the maltcms artifactory instance for artifact resolution (required for maven).

## Maven specifics:  
Please note that contrary to usual maven behaviour, integration test execution is 
currently disabled for default builds due to the long runtime of those.

To enable integration test execution, call maven with the argument -DskipITs=false
The build will run all standard tests without user-intervention.

The directory 'maltcms-test-tools' contains additional modules whose artifacts
are required by maltcms, but which are not automatically built with maltcms anymore
due to their size.

Additionally, due to the size of the project, it is not always possible to 
call multiple maven targets within one call. So instead of calling 
	
	mvn clean install 

please use

	mvn clean && mvn install

Or issue three separate calls of maven from the command-line:
	
	mvn clean
	mvn install

In order to build maltcms and have all tests run automatically, call:

	mvn -Dmaven.test.skip=false install

This will create a zip release of the project below maltcms-distribution/target.

## License Headers
Check, that all new files all have the maltcms license header, provided 
under src/main/resources/licenses/license-maltcms.txt. NetBeans IDE 
allows to add this header as a template for new files. Go to Tools->Templates->
Licenses and select Add in the Dialog. Go ahead and select the license header 
template. 

To add / update the license header, there is a nice plugin for the NetBeans IDE
available: 
	http://plugins.netbeans.org/plugin/17960/license-changer

The newest version supports NetBeans Templates automatically, so you can simply
select the 'maltcms' license from the dropdown menu of available license templates.

## Creating a release version *

Preparations:
1. Make sure that you have merged all upstream changes to be included in the release
	
	git pull origin master

2. Create a release branch (e.g. for release version 1.3.0):

	git checkout -b release-1.3.0

3. Bump the versions to the next release number, e.g. -RC1 for first release candidate, or 1.3.0 for the final version

	mvn versions:set -Dnew Version=1.3.0-RC1

4. Begin to prepare the site files, documentation and resources for the next release 
   version. Update the changes etc. 
   Generate a changelog from the first commit after the previous release until today
    
	mvn -DstartDate=YYYY-MM-DD -DendDate=YYYY-MM-DD scm:changelog > changelog.txt

   Update the site release notes
4.a) Merging intermediate changes from master into the branch (bugfixes only)

	git rebase master 

5. commit and push your changes 
	
	git push 

6. Replicate your branch to origin

	git push -u origin release-1.3.0


7. Run clean and install

    mvn -Dmaven.test.skip=false clean install

If anything fails, fix the bugs, add the changes and commit, before repeating the previous steps.

8. If everything works, deploy with src and javadoc profile enabled
    
    mvn deploy -Psrc,javadoc

Copy nbm artifacts to maltcms.de/updates/cross/RELEASE_VERSION/

9. Open an ssh connection to sourceforge:

    ssh -t username,maltcms@shell.sourceforge.net create

10. Build and stage the site:

    mvn site:site site:stage

11. If everything is fine, deploy the site

    mvn site:deploy

12. Create a release tag VERSION

	git tag -a 1.3.0

13. Push the tag to origin to share it

	git push origin 1.3.0

14. Change back to the master branch

	git checkout master

15. Apply changes of latest release branch to master

	git merge release-1.3.0

16. Update version numbers to next development version if release cycle is complete

	mvn versions:set -Dnew Version=1.4.0-SNAPSHOT

17. Commit and push changes

	git commit -m "Update project version to next development version."
	git push origin master

## Deploying a snapshot to the artifactory repository *

	mvn deploy


